package com.example.project.controller;


import com.example.project.repository.superadmin.ReservasRepository;
import com.example.project.repository.superadmin.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    SuperAdminRepository superAdminRepository;

    @Autowired
    ReservasRepository reservasRepository;

    @GetMapping(value = {"", "/home"})
    public String home(Model model) {
        System.out.println("--- DEBUG: Accediendo a /superadmin/home ---"); // DEBUG

        model.addAttribute("cantidadActivos", superAdminRepository.cantidadDeEspaciosPorIdEstado(1));
        model.addAttribute("cantidadEnMantenimiento", superAdminRepository.cantidadDeEspaciosPorIdEstado(2));
        model.addAttribute("cantidadPrestados", superAdminRepository.cantidadDeEspaciosPorIdEstado(3));
        model.addAttribute("cantidadCerrados", superAdminRepository.cantidadDeEspaciosPorIdEstado(4));
        model.addAttribute("cantidadTotal", superAdminRepository.count());
        model.addAttribute("listaUsuarios", superAdminRepository.findAll());
        model.addAttribute("listaReservas", reservasRepository.findAll());
        model.addAttribute("totalReservas", reservasRepository.count());
        model.addAttribute("ingresoTotal", reservasRepository.ingresoTotal());
        model.addAttribute("ingresoTotalEnLinea", reservasRepository.ingresoTotalEnLinea());
        model.addAttribute("ingresoTotalEnBanco", reservasRepository.ingresoTotalEnBanco());
        /*model.addAttribute("ingresoMes", reservasRepository.ingresoMensual());*/

        List<String> listaMeses = new ArrayList<>();
        List<Integer> listCantidadesMes = new ArrayList<>();

        System.out.println("--- DEBUG: Iniciando bucle para ingresos mensuales ---"); // DEBUG
        for (int i = 0; i < 7; i++) {
            Integer ingreso = reservasRepository.ingresoMensual(i);
            Integer monthInt = reservasRepository.intofmonth(i);
            System.out.println("--- DEBUG: Bucle i=" + i + ", Ingreso=" + ingreso + ", MonthInt=" + monthInt + " ---"); // DEBUG

            listCantidadesMes.add(ingreso);

            switch (monthInt){
                case 1:
                    listaMeses.add("Ene");
                    break;
                case 2:
                    listaMeses.add("Feb");
                    break;
                case 3:
                    listaMeses.add("Mar");
                    break;
                case 4:
                    listaMeses.add("Abr");
                    break;
                case 5:
                    listaMeses.add("May");
                    break;
                case 6:
                    listaMeses.add("Jun");
                    break;
                case 7:
                    listaMeses.add("Jul");
                    break;
                case 8:
                    listaMeses.add("Ago");
                    break;
                case 9:
                    listaMeses.add("Sep");
                    break;
                case 10:
                    listaMeses.add("Oct");
                    break;
                case 11:
                    listaMeses.add("Nov");
                    break;
                case 12:
                    listaMeses.add("Dic");
                    break;
                default:
                    System.out.println("--- DEBUG: MonthInt " + monthInt + " no coincide con ningún mes. No se añadió mes a listaMeses para i=" + i + " ---"); // DEBUG
                    // Si monthInt no coincide con ningún caso, no se añade nada a listaMeses.
                    // Esto es una posible causa de que listaMeses tenga menos elementos que listCantidadesMes.
            }
        }
        System.out.println("--- DEBUG: Tamaño de listCantidadesMes antes de añadir al modelo: " + listCantidadesMes.size() + " ---"); // DEBUG
        System.out.println("--- DEBUG: Tamaño de listaMeses antes de añadir al modelo: " + listaMeses.size() + " ---"); // DEBUG


        model.addAttribute("meses", listaMeses);
        model.addAttribute("cantidadesMes", listCantidadesMes);

        List<String> listaDias = new ArrayList<>();
        List<Integer> listaCantidadesBanco = new  ArrayList<>();
        List<Integer> listaCantidadesEnLinea = new ArrayList<>();

        System.out.println("--- DEBUG: Iniciando bucle para ingresos diarios ---"); // DEBUG
        for (int i = 0; i < 8; i++) {
            Integer ingresoBanco = reservasRepository.ingresoDiarioEnBanco(i);
            Integer ingresoEnLinea = reservasRepository.ingresoDiarioEnLinea(i);
            Integer dayInt = reservasRepository.intofday(i);
            System.out.println("--- DEBUG: Bucle i=" + i + ", IngresoBanco=" + ingresoBanco + ", IngresoEnLinea=" + ingresoEnLinea + ", DayInt=" + dayInt + " ---"); // DEBUG

            listaCantidadesBanco.add(ingresoBanco);
            listaCantidadesEnLinea.add(ingresoEnLinea);

            switch (dayInt){
                case 1:
                    listaDias.add("Dom");
                    break;
                case 2:
                    listaDias.add("Lun");
                    break;
                case 3:
                    listaDias.add("Mar");
                    break;
                case 4:
                    listaDias.add("Mié");
                    break;
                case 5:
                    listaDias.add("Jue");
                    break;
                case 6:
                    listaDias.add("Vie");
                    break;
                case 7:
                    listaDias.add("Sáb");
                    break;
                default:
                    System.out.println("--- DEBUG: DayInt " + dayInt + " no coincide con ningún día. No se añadió día a listaDias para i=" + i + " ---"); // DEBUG
                    // Si dayInt no coincide con ningún caso, no se añade nada a listaDias.
                    // Esto es una posible causa de que listaDias tenga menos elementos que las listas de cantidades.
            }
        }
        System.out.println("--- DEBUG: Tamaño de listaCantidadesBanco antes de añadir al modelo: " + listaCantidadesBanco.size() + " ---"); // DEBUG
        System.out.println("--- DEBUG: Tamaño de listaCantidadesEnLinea antes de añadir al modelo: " + listaCantidadesEnLinea.size() + " ---"); // DEBUG
        System.out.println("--- DEBUG: Tamaño de listaDias antes de añadir al modelo: " + listaDias.size() + " ---"); // DEBUG


        model.addAttribute("dias", listaDias);
        model.addAttribute("cantidadesBanco", listaCantidadesBanco);
        model.addAttribute("cantidadesEnLinea", listaCantidadesEnLinea);

        System.out.println("--- DEBUG: Retornando vista superadmin/casa ---"); // DEBUG
        return "superadmin/casa";
    }

    @GetMapping(value = "/en")
    public String habilita(@RequestParam("id") int id){
        superAdminRepository.actualizarEstadoUsuario(id, 1);
        return "redirect:/superadmin/home";
    }

    @GetMapping(value = "/de")
    public String deshabilita(@RequestParam("id") int id){
        superAdminRepository.actualizarEstadoUsuario(id, 2);
        return "redirect:/superadmin/home";
    }
}
