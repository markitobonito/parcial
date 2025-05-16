package com.example.project.controller;
import com.example.project.entity.Usuarios;
import com.example.project.repository.EstadoUsuRepository;
import com.example.project.repository.RolRepository;
import com.example.project.repository.UsuariosRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
            @RequestParam int dni,
            @RequestParam(required = false) String correo,
            @RequestParam String contrasena,
            @RequestParam String confirmContrasena) {

        if (!contrasena.equals(confirmContrasena)) {
            // aquí podrías reenviar un error al modelo; para simplificar:
            return "redirect:/registro?error=diff";
        }

        Usuarios u = new Usuarios();
        u.setDni(dni);
        u.setCorreo(correo);
        u.setContrasena(contrasena);

        // Asignar rol “vecino” y estado “activo”
        u.setRol( rolRepository.findByRol("Usuario final") );
        u.setEstado( estadoRepository.findByEstado("activo") );

        usuariosRepository.save(u);

        // redirige a tu login.html
        return "redirect:/login";
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
