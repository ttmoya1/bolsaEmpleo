package org.example.bolsaempleo.presentation.admin;

import org.example.bolsaempleo.logic.Caracteristica;
import org.example.bolsaempleo.logic.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class ControllerAdmin {

    @Autowired
    private Service service;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEmpresasPendientes", service.empresasPendientes().size());
        model.addAttribute("totalOferentesPendientes", service.oferentesPendientes().size());
        return "presentation/admin/ViewDashboard";
    }


    @GetMapping("/empresas")
    public String empresas(Model model) {
        model.addAttribute("pendientes", service.empresasPendientes());
        model.addAttribute("aprobadas",  service.empresasAprobadas());
        return "presentation/admin/ViewEmpresas";
    }

    @GetMapping("/empresas/aprobar/{id}")
    public String aprobarEmpresa(@PathVariable Long id) {
        service.aprobarEmpresa(id);
        return "redirect:/admin/empresas";
    }


    @GetMapping("/oferentes")
    public String oferentes(Model model) {
        model.addAttribute("pendientes", service.oferentesPendientes());
        model.addAttribute("aprobados",  service.oferentesAprobados());
        return "presentation/admin/ViewOferentes";
    }

    @GetMapping("/oferentes/aprobar/{id}")
    public String aprobarOferente(@PathVariable Long id) {
        service.aprobarOferente(id);
        return "redirect:/admin/oferentes";
    }


    private List<Caracteristica> buildRuta(Long nodoId) {
        if (nodoId == null) return Collections.emptyList();
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica actual = service.caracteristicaById(nodoId);
        while (actual != null) {
            ruta.add(0, actual);
            actual = actual.getPadre();
        }
        return ruta;
    }

    @GetMapping("/caracteristicas")
    public String caracteristicas(
            @RequestParam(value = "nodoId", required = false) Long nodoId,
            Model model) {

        List<Caracteristica> hijos = (nodoId == null)
                ? service.caracteristicasRaiz()
                : service.hijosDe(nodoId);

        List<Caracteristica> ruta     = buildRuta(nodoId);
        Caracteristica       nodoPadre = (nodoId != null) ? service.caracteristicaById(nodoId) : null;

        model.addAttribute("hijos",      hijos);
        model.addAttribute("ruta",       ruta);
        model.addAttribute("nodoId",     nodoId);
        model.addAttribute("nodoPadre",  nodoPadre);
        model.addAttribute("caracteristica", new Caracteristica());

        return "presentation/admin/ViewCaracteristicas";
    }

    @PostMapping("/caracteristicas/guardar")
    public String guardarCaracteristica(
            @RequestParam("nombre")                           String nombre,
            @RequestParam(value = "padreId", required = false) Long   padreId,
            @RequestParam(value = "nodoId",  required = false) Long   nodoId) {

        Caracteristica nueva = new Caracteristica();
        nueva.setNombre(nombre.trim());
        nueva.setPadre((padreId != null && padreId != 0L)
                ? service.caracteristicaById(padreId)
                : null);
        service.guardarCaracteristica(nueva);

        return (nodoId != null)
                ? "redirect:/admin/caracteristicas?nodoId=" + nodoId
                : "redirect:/admin/caracteristicas";
    }


    @GetMapping("/caracteristicas/confirmar-eliminar/{id}")
    public String confirmarEliminar(
            @PathVariable Long id,
            @RequestParam(value = "nodoId", required = false) Long nodoId,
            Model model) {

        model.addAttribute("caracteristica", service.caracteristicaById(id));
        model.addAttribute("nodoId", nodoId);
        return "presentation/admin/ViewConfirmarEliminarCaracteristica";
    }


    @PostMapping("/caracteristicas/eliminar/{id}")
    public String eliminarCaracteristica(
            @PathVariable Long id,
            @RequestParam(value = "nodoId", required = false) Long nodoId) {

        Long padreIdRedireccion = null;
        try {
            Caracteristica c = service.caracteristicaById(id);
            if (c.getPadre() != null) {
                padreIdRedireccion = c.getPadre().getId();
            }
        } catch (Exception ignored) {}

        service.eliminarCaracteristica(id);

        return (padreIdRedireccion != null)
                ? "redirect:/admin/caracteristicas?nodoId=" + padreIdRedireccion
                : "redirect:/admin/caracteristicas";
    }


    @GetMapping("/reporte")
    public String reporte(Model model) {
        model.addAttribute("datos", service.reportePuestosPorMes());
        return "presentation/admin/ViewReporte";
    }
}