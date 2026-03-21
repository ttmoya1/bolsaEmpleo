package org.example.bolsaempleo.presentation.oferente;

import org.example.bolsaempleo.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/oferente")
public class ControllerOferente {

    @Autowired
    private Service service;

    // Ruta donde se guardan los PDFs (dentro de resources/static o configurable)
    private static final String PDF_DIR = "src/main/resources/static/curricula/";

    private Oferente getOferente(UserDetails ud) {
        Usuario usuario = service.usuarioByCorreo(ud.getUsername());
        return service.oferenteByUsuario(usuario);
    }

    // ----------------------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Oferente oferente = getOferente(ud);
        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        return "presentation/oferente/ViewDashboard";
    }

    // ----------------------------------------------------------------
    // HABILIDADES  –  ver y editar
    // ----------------------------------------------------------------
    @GetMapping("/habilidades")
    public String habilidadesGet(@AuthenticationPrincipal UserDetails ud, Model model) {
        Oferente oferente = getOferente(ud);
        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        model.addAttribute("disponibles", service.caracteristicasHojas());
        return "presentation/oferente/ViewHabilidades";
    }

    @PostMapping("/habilidades/guardar")
    public String habilidadesPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(value = "caracIds", required = false) List<Long>    caracIds,
            @RequestParam(value = "niveles",  required = false) List<Integer> niveles) {

        Oferente oferente = getOferente(ud);

        List<OferenteHabilidad> lista = new ArrayList<>();
        if (caracIds != null) {
            for (int i = 0; i < caracIds.size(); i++) {
                OferenteHabilidad h = new OferenteHabilidad();
                h.setCaracteristica(service.caracteristicaById(caracIds.get(i)));
                h.setNivel(niveles != null && i < niveles.size() ? niveles.get(i) : 1);
                lista.add(h);
            }
        }
        service.guardarHabilidades(oferente, lista);
        return "redirect:/oferente/habilidades";
    }

    // ----------------------------------------------------------------
    // SUBIR CURRÍCULO PDF
    // ----------------------------------------------------------------
    @GetMapping("/curriculum")
    public String curriculumGet(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("oferente", getOferente(ud));
        return "presentation/oferente/ViewCurriculum";
    }

    @PostMapping("/curriculum/subir")
    public String curriculumPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("archivo") MultipartFile archivo,
            Model model) {

        if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
            model.addAttribute("error", "Debe seleccionar un archivo PDF válido.");
            model.addAttribute("oferente", getOferente(ud));
            return "presentation/oferente/ViewCurriculum";
        }

        Oferente oferente = getOferente(ud);
        try {
            Path dir = Paths.get(PDF_DIR);
            Files.createDirectories(dir);
            String nombreArchivo = "cv_" + oferente.getId() + ".pdf";
            Files.copy(archivo.getInputStream(),
                    dir.resolve(nombreArchivo),
                    StandardCopyOption.REPLACE_EXISTING);
            oferente.setCurriculumPdf("/curricula/" + nombreArchivo);
            service.guardarOferente(oferente);
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar el archivo: " + e.getMessage());
            model.addAttribute("oferente", oferente);
            return "presentation/oferente/ViewCurriculum";
        }
        return "redirect:/oferente/dashboard";
    }

    // ----------------------------------------------------------------
    // BUSCAR PUESTOS (PUB + PRI para oferentes aprobados)
    // ----------------------------------------------------------------
    @GetMapping("/buscar")
    public String buscarGet(Model model,
                            @RequestParam(defaultValue = "") String texto) {
        model.addAttribute("texto",      texto);
        model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        return "presentation/oferente/ViewBuscarPuestos";
    }

    @PostMapping("/buscar")
    public String buscarPost(Model model,
                             @RequestParam(defaultValue = "") String texto) {
        model.addAttribute("texto",      texto);
        model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        return "presentation/oferente/ViewBuscarPuestos";
    }
}