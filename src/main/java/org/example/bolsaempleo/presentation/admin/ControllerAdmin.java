package org.example.bolsaempleo.presentation.admin;

import jakarta.validation.Valid;
import org.example.bolsaempleo.logic.Caracteristica;
import org.example.bolsaempleo.logic.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class ControllerAdmin {

    @Autowired
    private Service service;

    // ----------------------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEmpresasPendientes", service.empresasPendientes().size());
        model.addAttribute("totalOferentesPendientes", service.oferentesPendientes().size());
        return "presentation/admin/ViewDashboard";
    }

    // ----------------------------------------------------------------
    // EMPRESAS PENDIENTES
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    // OFERENTES PENDIENTES
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    // CARACTERÍSTICAS  –  navegación por árbol (igual que oferente)
    // ----------------------------------------------------------------

    /**
     * Construye la ruta de breadcrumb desde la raíz hasta el nodo actual.
     */
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

        // Hijos a mostrar en el árbol central
        List<Caracteristica> hijos;
        if (nodoId == null) {
            hijos = service.caracteristicasRaiz();
        } else {
            hijos = service.hijosDe(nodoId);
        }

        // Ruta del breadcrumb
        List<Caracteristica> ruta = buildRuta(nodoId);

        // Nodo actual (para preseleccionar el padre en el formulario)
        Caracteristica nodoPadre = (nodoId != null) ? service.caracteristicaById(nodoId) : null;

        model.addAttribute("hijos",      hijos);
        model.addAttribute("ruta",       ruta);
        model.addAttribute("nodoId",     nodoId);
        model.addAttribute("nodoPadre",  nodoPadre);
        model.addAttribute("caracteristica", new Caracteristica());

        return "presentation/admin/ViewCaracteristicas";
    }

    @PostMapping("/caracteristicas/guardar")
    public String guardarCaracteristica(
            @RequestParam(value = "nombre") String nombre,
            @RequestParam(value = "padreId", required = false) Long padreId,
            @RequestParam(value = "nodoId",  required = false) Long nodoId) {

        Caracteristica nueva = new Caracteristica();
        nueva.setNombre(nombre.trim());

        if (padreId != null && padreId != 0L) {
            nueva.setPadre(service.caracteristicaById(padreId));
        } else {
            nueva.setPadre(null);
        }

        service.guardarCaracteristica(nueva);

        // Redirige al mismo nivel donde se hizo la acción
        if (nodoId != null) {
            return "redirect:/admin/caracteristicas?nodoId=" + nodoId;
        }
        return "redirect:/admin/caracteristicas";
    }

    @GetMapping("/caracteristicas/eliminar/{id}")
    public String eliminarCaracteristica(
            @PathVariable Long id,
            @RequestParam(value = "nodoId", required = false) Long nodoId) {

        // Antes de eliminar, guardamos el padre para redirigir al nivel correcto
        Long padreIdRedireccion = null;
        try {
            Caracteristica c = service.caracteristicaById(id);
            if (c.getPadre() != null) {
                padreIdRedireccion = c.getPadre().getId();
            }
        } catch (Exception ignored) {}

        service.eliminarCaracteristica(id);

        // Si venía navegando desde un nodo padre, volver a ese nivel
        if (padreIdRedireccion != null) {
            return "redirect:/admin/caracteristicas?nodoId=" + padreIdRedireccion;
        }
        return "redirect:/admin/caracteristicas";
    }

    // ----------------------------------------------------------------
    // REPORTE PDF puestos por mes
    // ----------------------------------------------------------------
    @GetMapping("/reporte")
    public String reporte(Model model) {
        model.addAttribute("datos", service.reportePuestosPorMes());
        return "presentation/admin/ViewReporte";
    }
}