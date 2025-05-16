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
@RequestMapping("/vecino")
public class VecinoController {
    final UsuariosRepository usuariosRepository;
    final RolRepository rolRepository;
    final EstadoUsuRepository estadoRepository;// Declaración correcta
    // Constructor con inyección de dependencia
    public VecinoController(UsuariosRepository usuariosRepository,
                            RolRepository rolRepository,
                            EstadoUsuRepository estadoRepository) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository      = rolRepository;
        this.estadoRepository   = estadoRepository;
    }
    @GetMapping("/home")
    public String principal(Model model) {

        return "registro/principal"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/pago")
    public String pago(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/pago"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/disponibilidad")
    public String disponibilidad(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/vecino-disponibilidad-espacio"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/resumen")
    public String resumen(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/vecino-resumen-reserva"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/detalles")
    public String detalles(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/vecino-ver-detalles-espacio"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/verEspacios")
    public String verEspacios(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/vecino-ver-espacios"; // Usa la misma vista para mostrar todos los DNIs
    }
}
