package com.example.project.controller;
import com.example.project.entity.Espacio;
import com.example.project.entity.Reserva;
import com.example.project.entity.Usuarios;
import com.example.project.repository.EstadoUsuRepository;
import com.example.project.repository.RolRepository;
import com.example.project.repository.UsuariosRepository;
import com.example.project.repository.admin.EspacioRepository;
import com.example.project.repository.admin.ReservaRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.Inet4Address;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vecino")
public class VecinoController {
    final UsuariosRepository usuariosRepository;
    final RolRepository rolRepository;
    final EstadoUsuRepository estadoRepository;// Declaración correcta
    final EspacioRepository espacioRepository;
    final ReservaRepository reservaRepository;
    // Constructor con inyección de dependencia
    public VecinoController(UsuariosRepository usuariosRepository,
                            RolRepository rolRepository,
                            EstadoUsuRepository estadoRepository,
                            EspacioRepository espacioRepository,
                            ReservaRepository reservaRepository) {
        this.usuariosRepository = usuariosRepository;
        this.rolRepository      = rolRepository;
        this.estadoRepository   = estadoRepository;
        this.espacioRepository = espacioRepository;
        this.reservaRepository = reservaRepository;
    }
    @GetMapping("/home")
    public String principal(Model model) {

        return "registro/principal"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/pago")
    public String pago(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        List<Espacio> espacios = espacioRepository.findAll();
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/pago"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/disponibilidad")
    public String disponibilidad(@RequestParam("id") int id,Model model) {

        Optional<Espacio> espacio = espacioRepository.findById(id);
        if(espacio.isPresent()) {
            model.addAttribute("espacio", espacio.get());
            List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
            model.addAttribute("usuarios", usuarios);// Pasa la lista a la vista
            return "vecino/vecino-disponibilidad-espacio";
        }
        else {
            return "redirect:/vecino/home";
        }
    }

    @GetMapping("/resumen")
    public String resumen(@AuthenticationPrincipal Usuarios user,Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        /*if(user.getIdUsuarios()==null){
            user.setIdUsuarios(1);
        }
        Integer id= user.getIdUsuarios();
        if(id==null){
            id=1;
        }*/
        int id=1;
        List<Reserva> reservas = reservaRepository.findByIDUsuario(id);
        model.addAttribute("reservas", reservas);
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        return "vecino/vecino-resumen-reserva"; // Usa la misma vista para mostrar todos los DNIs
    }

    @GetMapping("/detalles")
    public String detalles(@RequestParam("id") int id,Model model) {
        Optional<Espacio> espacio = espacioRepository.findById(id);
        if(espacio.isPresent()) {
            model.addAttribute("espacio", espacio.get());
            List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
            model.addAttribute("usuarios", usuarios);// Pasa la lista a la vista
            return "vecino/vecino-ver-detalles-espacio";
        }
        else {
            return "redirect:/vecino/home";
        }
    }

    @GetMapping("/verEspacios")
    public String verEspacios(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll(); // Obtiene todos los usuarios
        List<Espacio> espacios = espacioRepository.findAll();
        model.addAttribute("usuarios", usuarios); // Pasa la lista a la vista
        model.addAttribute("espacios", espacios);
        return "vecino/vecino-ver-espacios"; // Usa la misma vista para mostrar todos los DNIs
    }

    @PostMapping("/reserva/save")
    public String fechaHoraSplit(Reserva reserva,
                                 @RequestParam("dateTimeIni") String dateTimeIni,
                                 @RequestParam("dateTimeFin") String dateTimeFin,
                                 Model model) {
        String[] dateTime= dateTimeIni.split(" ");
        String[] dateTimeF= dateTimeFin.split(" ");
        String fecha=dateTime[0];
        String hora=dateTime[1]+":00";
        String horaF=dateTimeF[1]+":00";
        System.out.println(fecha+hora+horaF);
        Date fechaDate = Date.valueOf(fecha);
        Time horaTime = Time.valueOf(hora);
        reserva.setFecha(fechaDate);
        reserva.setHoraInicio(horaTime);
        Time horaFTime = Time.valueOf(horaF);
        reserva.setHoraFin(horaFTime);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        reserva.setMomentoReserva(timestamp);
        reservaRepository.save(reserva);
        return "redirect:/vecino/home";
    }
}
