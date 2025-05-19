package com.example.project.controller;

import com.example.project.entity.EstadoUsu;
import com.example.project.entity.Rol;
import com.example.project.entity.Usuarios;
import com.example.project.repository.EstadoUsuRepository;
import com.example.project.repository.RolRepository;
import com.example.project.repository.UsuariosRepository;
import com.example.project.service.MailManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ¡Importar para hashear!
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // Necesario para generar un token "ficticio" para el enlace

@Controller
@RequestMapping("/")
public class HomeController {
    final UsuariosRepository usuariosRepository;
    final RolRepository rolRepository;
    final EstadoUsuRepository estadoRepository;
    final MailManager mailManager;
    final PasswordEncoder passwordEncoder; // Inyección de BCryptPasswordEncoder

    @Value("${app.base-url}") // Inyecta la URL base desde application.properties
    private String appBaseUrl;

    // Constructor con las inyecciones necesarias
    public HomeController(UsuariosRepository usuariosRepository,
                          RolRepository rolRepository,
                          EstadoUsuRepository estadoRepository,
                          MailManager mailManager,
                          PasswordEncoder passwordEncoder) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository = rolRepository;
        this.estadoRepository = estadoRepository;
        this.mailManager = mailManager;
        this.passwordEncoder = passwordEncoder;
    }

    // --- MÉTODOS EXISTENTES (mantener sin cambios si ya funcionan) ---
    @GetMapping("/")
    public String index() {
        return "registro/principal";
    }

    @GetMapping("/login")
    public String login() {
        return "registro/login";
    }

    @GetMapping("/registro")
    public String registrarUsuario(Model model) {
        model.addAttribute("usuario", new Usuarios());
        return "registro/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuarios usuario,
            BindingResult bindingResult,
            Model model) {
        String dniString = String.valueOf(usuario.getDni());
        if (dniString.length() != 8) {
            bindingResult.addError(new FieldError("usuario", "dni", usuario.getDni(), false, new String[]{"usuario.dni.size"}, null, "El DNI debe tener exactamente 8 n\u00FAmeros."));
        } else if (!dniString.matches("\\d{8}")) {
            bindingResult.addError(new FieldError("usuario", "dni", usuario.getDni(), false, new String[]{"usuario.dni.digits"}, null, "El DNI solo puede contener n\u00FAmeros."));
        }

        if (!usuario.getContrasena().equals(usuario.getConfirmContrasena())) {
            bindingResult.addError(new FieldError("usuario", "confirmContrasena", "Las contraseñas no coinciden."));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "registro/registro";
        }

        Rol rolUsuario = rolRepository.findByRol("Usuario final");
        EstadoUsu estadoActivo = estadoRepository.findByEstado("activo");

        usuario.setRol(rolUsuario);
        usuario.setEstado(estadoActivo);
        // ¡ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDARLA AL REGISTRAR!
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        usuariosRepository.save(usuario);

        return "redirect:/login";
    }
    // --- FIN MÉTODOS EXISTENTES ---


    // --- MÉTODOS PARA RESTABLECER CONTRASEÑA ---

    @GetMapping("/olvidoContrasena")
    public String olvidoContrasena() {
        return "registro/olvidoContrasena"; // Asegúrate de que esta vista exista
    }

    @PostMapping("/olvido")
    public String procesarRecuperarContrasena(@RequestParam("email") String identificador,
                                              Model model,
                                              RedirectAttributes redirectAttributes) {
        String identificadorLimpio = identificador.trim().replaceAll("\\s+", "");

        boolean esSoloNumeros = true;
        if (identificadorLimpio.isEmpty()) {
            esSoloNumeros = false;
        } else {
            for (char c : identificadorLimpio.toCharArray()) {
                if (!Character.isDigit(c)) {
                    esSoloNumeros = false;
                    break;
                }
            }
        }

        if (esSoloNumeros) {
            // Flujo para DNI/Teléfono (OTP) - Si este flujo existe y no quieres cambiarlo
            redirectAttributes.addFlashAttribute("identificador", identificadorLimpio);
            return "registro/verificarNumero"; // Asegúrate de que esta URL exista
        } else {
            // Flujo para Correo Electrónico
            String emailDestino = identificadorLimpio;

            // Generamos un token UUID simple. Este token NO se guarda en DB ni se valida.
            // Es solo para que el enlace se vea único y evitar que se use siempre el mismo.
            String token = UUID.randomUUID().toString();

            // Construimos el enlace de restablecimiento. ¡Enviamos el EMAIL en la URL!
            String resetLink = appBaseUrl + "/renovarContrasena?token=" + token + "&email=" + emailDestino;

            String emailContent = "Estimado(a) usuario,<br><br>" +
                    "Para restablecer tu contraseña en el Sistema de Reservas de Canchas de la Municipalidad de San Miguel, " +
                    "haz clic en el siguiente enlace:<br>" +
                    "<a href=\"" + resetLink + "\">Haz clic aquí para restablecer tu contraseña</a>" +
                    "<br><br><b>Importante:</b> Este enlace te llevará a la página para ingresar tu nueva contraseña. " +
                    "Por favor, **vuelve a ingresar tu correo electrónico** o DNI en el formulario para confirmar tu identidad y actualizar tu contraseña." +
                    "<br><br>Si no solicitaste un restablecimiento de contraseña, por favor, ignora este correo." +
                    "<br><br>Saludos cordiales,<br>El equipo de la Municipalidad de San Miguel.";

            // Enviamos el correo. Por seguridad, no informamos si el email no existe.
            mailManager.sendMessage(emailDestino, "Restablecimiento de Contraseña - Municipalidad de San Miguel", emailContent);

            model.addAttribute("identificador", identificadorLimpio); // Pasa el correo a la vista
            return "registro/verificarEmail"; // Plantilla que dice "Revisa tu email"
        }
    }

    @GetMapping("/renovarContrasena")
    public String formularioContrasena(@RequestParam(value = "token", required = false) String token, // Recibimos el token "ficticio"
                                       @RequestParam(value = "email", required = false) String email, // Recibimos el email enviado en la URL
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        // Validamos que el token y el email estén presentes en la URL.
        // No validamos el "token" contra la DB porque no lo guardamos.
        if (token == null || token.isEmpty() || email == null || email.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace de restablecimiento de contraseña es inválido o incompleto. Por favor, solicita uno nuevo.");
            return "redirect:/olvidoContrasena";
        }

        model.addAttribute("token", token); // Pasamos el token a la vista (puede ser útil para scripts, aunque no se valide en el backend)
        model.addAttribute("identificadorRecuperado", email); // Pasamos el email para rellenar el campo del formulario

        return "registro/nuevaContrasena"; // Tu formulario para la nueva contraseña
    }

    @Autowired
    private JdbcTemplate jdbcTemplate; // Inyectamos el JdbcTemplate

    @PostMapping("/confirmoContrasena")
    @Transactional
    public String procesarNuevaContrasena(
            @RequestParam("identificador") String identificador,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "token", required = false) String token,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Validar que las contraseñas coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden. Por favor, inténtalo de nuevo.");
                return "redirect:/renovarContrasena?token=" + (token != null ? token : "") + "&email=" + identificador;
            }

            // 2. Validar longitud de la contraseña
            if (newPassword.length() < 3 || newPassword.length() > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "La contraseña debe tener entre 3 y 100 caracteres.");
                return "redirect:/renovarContrasena?token=" + (token != null ? token : "") + "&email=" + identificador;
            }

            // 3. Hashear la nueva contraseña
            String hashedPassword = passwordEncoder.encode(newPassword);

            // 4. Determinar si el identificador es un correo o un DNI y construir la consulta SQL
            String sql;
            Object[] params;
            boolean esSoloNumeros = identificador.matches("\\d+");

            if (esSoloNumeros) {
                sql = "UPDATE Usuarios SET contrasena = ? WHERE dni = ?";
                params = new Object[]{hashedPassword, Integer.parseInt(identificador)};
            } else {
                sql = "UPDATE Usuarios SET contrasena = ? WHERE correo = ?";
                params = new Object[]{hashedPassword, identificador};
            }

            // 5. Ejecutar la consulta SQL dentro de la transacción
            int filasActualizadas = jdbcTemplate.update(sql, params);

            if (filasActualizadas > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "¡Tu contraseña ha sido restablecida exitosamente! Ya puedes iniciar sesión.");
                return "redirect:/login";
            } else {
                // Si no se actualizó ninguna fila, el usuario no existe
                redirectAttributes.addFlashAttribute("errorMessage", "El identificador (correo o DNI) proporcionado no está registrado.");
                return "redirect:/renovarContrasena?token=" + (token != null ? token : "") + "&email=" + identificador;
            }

        } catch (Exception e) {
            // Capturar cualquier error, incluyendo DataAccessException (que incluye SQLException)
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al restablecer la contraseña: " + e.getMessage());
            return "redirect:/renovarContrasena?token=" + (token != null ? token : "") + "&email=" + identificador;
        }
    }
    // Tu @PostMapping("/renovarContrasena") original para OTP, sin cambios
    @PostMapping("/renovarContrasena")
    public String procesarVerificacionOtp(@RequestParam("otp") String otpCode) {
        System.out.println("Código OTP recibido (flujo OTP): " + otpCode);
        return "registro/nuevaContrasena"; // O la vista que corresponda para el OTP
    }
}