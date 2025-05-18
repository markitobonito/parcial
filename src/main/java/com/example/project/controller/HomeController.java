package com.example.project.controller;
import com.example.project.entity.EstadoUsu;
import com.example.project.entity.Rol;
import com.example.project.entity.Usuarios;
import com.example.project.repository.EstadoUsuRepository;
import com.example.project.repository.RolRepository;
import com.example.project.repository.UsuariosRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {
    final UsuariosRepository usuariosRepository;
    final RolRepository rolRepository;
    final EstadoUsuRepository estadoRepository;// Declaración correcta
    // Constructor con inyección de dependencia
    public HomeController(UsuariosRepository usuariosRepository,
     RolRepository rolRepository,
                          EstadoUsuRepository estadoRepository) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository      = rolRepository;
        this.estadoRepository   = estadoRepository;
    }
    @GetMapping("/")
    public String pagPrincipal() {

        return "registro/principal"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/prueba")
    public String mostrarTodosUsuarios(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "hola"; // Usa la misma vista para mostrar todos los DNIs
    }



    @GetMapping("/login")
    public String login() {

        return "registro/login"; // Usa la misma vista para mostrar todos los DNIs
    }


    @GetMapping("/registro")
    public String mostrarFormRegistro(Model model) {
        model.addAttribute("usuario", new Usuarios());
        return "registro/registro";        // tu registro.html
    }



    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuarios usuario, // <-- ¡Aquí aplicamos @Valid!
            BindingResult bindingResult, // <-- ¡BindingResult inmediatamente después de @Valid!
            Model model) { // Pasamos el Model para agregar atributos si necesitamos volver a la vista
        String dniString = String.valueOf(usuario.getDni());
        if (dniString.length() != 8) {
            bindingResult.addError(new FieldError("usuario", "dni", usuario.getDni(), false, new String[]{"usuario.dni.size"}, null, "El DNI debe tener exactamente 8 n\u00FAmeros."));
        } else if (!dniString.matches("\\d{8}")) { // Verifica que solo sean dígitos
            bindingResult.addError(new FieldError("usuario", "dni", usuario.getDni(), false, new String[]{"usuario.dni.digits"}, null, "El DNI solo puede contener n\u00FAmeros."));
        }
        // 1. Validaciones de Bean Validation (automáticas por @Valid)
        // 2. Validación manual de contraseña (confirmContrasena)
        if (!usuario.getContrasena().equals(usuario.getConfirmContrasena())) {
            // Añadir un error al BindingResult para que Thymeleaf lo detecte
            bindingResult.addError(new FieldError("usuario", "confirmContrasena", "Las contraseñas no coinciden.")); // Mensaje directo
            // O mejor, usar el mensaje del properties:
            // bindingResult.addError(new FieldError("usuario", "confirmContrasena", "", false, new String[]{"usuario.contrasena.mismatch"}, null, "Las contraseñas no coinciden."));
        }

        // Si hay errores de validación (ya sean por Bean Validation o por la contraseña que no coincide)
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario); // Vuelve a enviar el objeto 'usuario' con los datos ingresados
            return "registro/registro"; // Vuelve a la misma vista para mostrar los errores
        }

        // Si todas las validaciones pasan:
        // Asignar rol “vecino” y estado “activo”
        Rol rolUsuario = rolRepository.findByRol("Usuario final");
        EstadoUsu estadoActivo = estadoRepository.findByEstado("activo");

        usuario.setRol(rolUsuario);
        usuario.setEstado(estadoActivo);
        // La contraseña ya viene encriptada si la haces en el servicio, o aquí si no.
        // ¡Importante: Encripta la contraseña antes de guardarla!
        // Ejemplo (necesitarías un PasswordEncoder bean):
        // usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        usuariosRepository.save(usuario);

        return "redirect:/login"; // Redirige al login si el registro fue exitoso
    }


    @GetMapping("/olvidoContrasena")
    public String mostrarFormularioOlvidoContrasena() {
        // Asumiendo que tu archivo HTML está en src/main/resources/templates/registro/olvidoContraseña.html
        // La cadena devuelta debe coincidir con la ruta desde 'templates/' (sin la extensión .html)
        return "registro/olvidoContrasena";
    }

    @PostMapping("/olvido")
    public String procesarRecuperarContrasena(@RequestParam("email") String identificador,
                                              Model model) { // El nombre del parámetro aquí puede ser cualquiera, pero el @RequestParam debe coincidir con el name del input
        // Limpiar posibles espacios en blanco al inicio o fin
        String identificadorLimpio = identificador.trim().replaceAll("\\s+", "");
        model.addAttribute("identificador", identificadorLimpio);
        // Verificar si el identificador contiene solo números
        boolean esSoloNumeros = true;
        if (identificadorLimpio.isEmpty()) {
            esSoloNumeros = false; // Considerar un campo vacío como no numérico o manejarlo como error
        } else {
            for (char c : identificadorLimpio.toCharArray()) {
                if (!Character.isDigit(c)) {
                    esSoloNumeros = false;
                    break; // Sale del bucle si encuentra algo que no sea un dígito
                }
            }
        }

        // Redirigir a la vista correspondiente
        if (esSoloNumeros) {
            // Si son puros números, va a la vista para verificar número (DNI/Teléfono)
            return "registro/verificarNumero"; // Devuelve el nombre de la plantilla (ubicada en src/main/resources/templates/registro/verificarNumero.html)
        } else {
            // Si no son puros números, asume que es un correo y va a la vista para verificar email
            return "registro/verificarEmail"; // Devuelve el nombre de la plantilla (ubicada en src/main/resources/templates/registro/VerificarEmail.html)
        }
    }


    @PostMapping("/confirmoContrasena")
    public String comfirm(@RequestParam("confirmPassword") String confirmPass,
                          @RequestParam("newPassword") String newPass) { // Recibe el parámetro 'otp'

        return "redirect:/login";
    }

    @PostMapping("/renovarContrasena")
    public String procesarVerificacionOtp(@RequestParam("otp") String otpCode) { // Recibe el parámetro 'otp'

        // Guardar el código OTP recibido en una variable local
        String receivedOtp = otpCode;

        // Aquí podrías (en el futuro) agregar lógica para:
        // - Buscar al usuario basado en el número/correo original (que necesitarías pasar de alguna forma, quizás en la sesión o como otro parámetro oculto).
        // - Verificar si el otpCode recibido coincide con el que le enviaste al usuario.
        // - Invalidar el código OTP después de su uso.
        // - Si el OTP es válido, permitirle al usuario proceder a cambiar su contraseña.

        // Por ahora, solo guardar y redirigir a la página de nueva contraseña
        System.out.println("Código OTP recibido: " + receivedOtp); // Opcional: imprimir para verificar

        // Redirigir a la página para establecer la nueva contraseña
        // Spring buscará la plantilla en src/main/resources/templates/registro/nuevaContrasena.html
        return "registro/nuevaContrasena";
    }

}
