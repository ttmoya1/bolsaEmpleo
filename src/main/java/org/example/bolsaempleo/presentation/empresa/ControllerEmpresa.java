package org.example.bolsaempleo.presentation.empresa;

import org.example.bolsaempleo.logic.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/empresa")
public class ControllerEmpresa {

    @Autowired
    private Service service;

    private Empresa getEmpresa(UserDetails ud) {
        Usuario usuario = service.usuarioByCorreo(ud.getUsername());
        return service.empresaByUsuario(usuario);
    }

    // ----------------------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Empresa empresa = getEmpresa(ud);
        model.addAttribute("empresa", empresa);
        model.addAttribute("puestos", service.todosPuestosByEmpresa(empresa));
        return "presentation/empresa/ViewDashboard";
    }

    // ----------------------------------------------------------------
    // PUESTOS  –  listar
    // ----------------------------------------------------------------
    @GetMapping("/puestos")
    public String puestos(@AuthenticationPrincipal UserDetails ud, Model model) {
        Empresa empresa = getEmpresa(ud);
        model.addAttribute("puestos", service.todosPuestosByEmpresa(empresa));
        return "presentation/empresa/ViewPuestos";
    }

    // ----------------------------------------------------------------
    // PUESTOS  –  crear
    // ----------------------------------------------------------------
    @GetMapping("/puestos/nuevo")
    public String nuevoPuestoGet(Model model) {
        model.addAttribute("puesto", new Puesto());
        model.addAttribute("caracteristicasDisponibles", service.caracteristicasHojas());
        model.addAttribute("editing", false);
        return "presentation/empresa/ViewEditPuesto";
    }

    @PostMapping("/puestos/crear")
    public String crearPuesto(
            @AuthenticationPrincipal UserDetails ud,
            @ModelAttribute("puesto") @Valid Puesto puesto, BindingResult result,
            @RequestParam(value = "caracIds",   required = false) List<Long>    caracIds,
            @RequestParam(value = "niveles",    required = false) List<Integer> niveles,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("caracteristicasDisponibles", service.caracteristicasHojas());
            model.addAttribute("editing", false);
            return "presentation/empresa/ViewEditPuesto";
        }

        Empresa empresa = getEmpresa(ud);
        puesto.setEmpresa(empresa);
        service.guardarPuesto(puesto);
        guardarCaracteristicasPuesto(puesto, caracIds, niveles);
        return "redirect:/empresa/puestos";
    }

    // ----------------------------------------------------------------
    // PUESTOS  –  editar
    // ----------------------------------------------------------------
    @GetMapping("/puestos/editar/{id}")
    public String editarPuestoGet(@PathVariable Long id, Model model) {
        model.addAttribute("puesto", service.puestoById(id));
        model.addAttribute("caracteristicasDisponibles", service.caracteristicasHojas());
        model.addAttribute("requeridas", service.caracteristicasByPuesto(id));
        model.addAttribute("editing", true);
        return "presentation/empresa/ViewEditPuesto";
    }

    @PostMapping("/puestos/actualizar")
    public String actualizarPuesto(
            @ModelAttribute("puesto") @Valid Puesto puesto, BindingResult result,
            @RequestParam(value = "caracIds",   required = false) List<Long>    caracIds,
            @RequestParam(value = "niveles",    required = false) List<Integer> niveles,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("caracteristicasDisponibles", service.caracteristicasHojas());
            model.addAttribute("editing", true);
            return "presentation/empresa/ViewEditPuesto";
        }

        Puesto original = service.puestoById(puesto.getId());
        puesto.setEmpresa(original.getEmpresa());
        service.guardarPuesto(puesto);
        guardarCaracteristicasPuesto(puesto, caracIds, niveles);
        return "redirect:/empresa/puestos";
    }

    // ----------------------------------------------------------------
    // PUESTOS  –  desactivar
    // ----------------------------------------------------------------
    @GetMapping("/puestos/desactivar/{id}")
    public String desactivarPuesto(@PathVariable Long id) {
        service.desactivarPuesto(id);
        return "redirect:/empresa/puestos";
    }

    // ----------------------------------------------------------------
    // BUSCAR CANDIDATOS para un puesto
    // ----------------------------------------------------------------
    @GetMapping("/puestos/{id}/candidatos")
    public String candidatos(@PathVariable Long id, Model model,
                             @RequestParam(defaultValue = "1") long minCoincidencias) {
        Puesto puesto = service.puestoById(id);
        List<Object[]> resultados = service.buscarCandidatos(id, minCoincidencias);
        model.addAttribute("puesto",           puesto);
        model.addAttribute("resultados",       resultados);
        model.addAttribute("minCoincidencias", minCoincidencias);
        model.addAttribute("totalRequisitos",  service.caracteristicasByPuesto(id).size());
        model.addAttribute("aplicaciones",     service.aplicacionesByPuesto(id));
        return "presentation/empresa/ViewCandidatos";
    }

    // ----------------------------------------------------------------
    // APLICACIONES  –  cambiar estado
    // ----------------------------------------------------------------
    @GetMapping("/aplicaciones/estado/{id}/{estado}")
    public String cambiarEstado(@PathVariable Long id, @PathVariable String estado) {
        Aplicacion apl = service.aplicacionById(id);
        service.cambiarEstadoAplicacion(id, estado);
        return "redirect:/empresa/puestos/" + apl.getPuesto().getId() + "/candidatos";
    }

    // ----------------------------------------------------------------
    // VER DETALLE DE UN CANDIDATO (oferente)
    // ----------------------------------------------------------------
    @GetMapping("/candidato/{id}")
    public String verCandidato(@PathVariable Long id, Model model) {
        Oferente oferente = service.oferenteById(id);
        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(id));
        return "presentation/empresa/ViewDetalleCandidato";
    }

    // ----------------------------------------------------------------
    // Auxiliar
    // ----------------------------------------------------------------
    private void guardarCaracteristicasPuesto(Puesto puesto,
                                              List<Long> caracIds,
                                              List<Integer> niveles) {
        List<PuestoCaracteristica> lista = new ArrayList<>();
        if (caracIds != null) {
            for (int i = 0; i < caracIds.size(); i++) {
                PuestoCaracteristica pc = new PuestoCaracteristica();
                pc.setCaracteristica(service.caracteristicaById(caracIds.get(i)));
                pc.setNivelDeseado(niveles != null && i < niveles.size() ? niveles.get(i) : 1);
                lista.add(pc);
            }
        }
        service.guardarCaracteristicasPuesto(puesto, lista);
    }
}