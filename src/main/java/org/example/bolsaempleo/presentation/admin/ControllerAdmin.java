package org.example.bolsaempleo.presentation.admin;


import jakarta.validation.Valid;
import org.example.bolsaempleo.logic.Caracteristica;
import org.example.bolsaempleo.logic.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    // CARACTERÍSTICAS
    // ----------------------------------------------------------------
    @GetMapping("/caracteristicas")
    public String caracteristicas(Model model) {
        model.addAttribute("raices",       service.caracteristicasRaiz());
        model.addAttribute("caracteristica", new Caracteristica());
        model.addAttribute("padres",       service.caracteristicasRaiz());
        return "presentation/admin/ViewCaracteristicas";
    }

    @PostMapping("/caracteristicas/guardar")
    public String guardarCaracteristica(
            @ModelAttribute("caracteristica") @Valid Caracteristica caracteristica,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("raices", service.caracteristicasRaiz());
            model.addAttribute("padres", service.caracteristicasRaiz());
            return "presentation/admin/ViewCaracteristicas";
        }
        // Si padreId viene como 0 significa que es raíz → padre null
        if (caracteristica.getPadre() != null && caracteristica.getPadre().getId() == 0) {
            caracteristica.setPadre(null);
        }
        service.guardarCaracteristica(caracteristica);
        return "redirect:/admin/caracteristicas";
    }

    @GetMapping("/caracteristicas/eliminar/{id}")
    public String eliminarCaracteristica(@PathVariable Long id) {
        service.eliminarCaracteristica(id);
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