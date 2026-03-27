package org.example.bolsaempleo.logic;

import org.example.bolsaempleo.logic.*;
import org.example.bolsaempleo.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@org.springframework.stereotype.Service
public class Service {

    @Autowired private UsuarioRepository          usuarios;
    @Autowired private EmpresaRepository          empresas;
    @Autowired private OferenteRepository         oferentes;
    @Autowired private CaracteristicaRepository   caracteristicas;
    @Autowired private PuestoRepository           puestos;
    @Autowired private PuestoCaracteristicaRepository puestosCaracteristicas;
    @Autowired private OferenteHabilidadRepository    habilidades;
    @Autowired private PasswordEncoder            passwordEncoder;

    // ================================================================
    // USUARIO
    // ================================================================

    public Usuario usuarioByCorreo(String correo) {
        return usuarios.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + correo));
    }

    @Autowired private AplicacionRepository aplicaciones;

    public void aplicarAPuesto(Long puestoId, Long oferenteId) {
        if (aplicaciones.existsByPuestoIdAndOferenteId(puestoId, oferenteId))
            throw new IllegalArgumentException("Ya aplicó a este puesto.");
        Aplicacion a = new Aplicacion();
        a.setPuesto(puestoById(puestoId));
        a.setOferente(oferenteById(oferenteId));
        aplicaciones.save(a);
    }

    public List<Aplicacion> aplicacionesByPuesto(Long puestoId) {
        return aplicaciones.findByPuestoId(puestoId);
    }

    public List<Aplicacion> aplicacionesByOferente(Long oferenteId) {
        return aplicaciones.findByOferenteId(oferenteId);
    }

    public void cambiarEstadoAplicacion(Long aplicacionId, String estado) {
        Aplicacion a = aplicaciones.findById(aplicacionId)
                .orElseThrow(() -> new IllegalArgumentException("Aplicación no encontrada."));
        a.setEstado(estado);
        aplicaciones.save(a);
    }

    // ================================================================
    // REGISTRO  (parte pública)
    // ================================================================
    public void registrarEmpresa(Usuario usuario, Empresa empresa) {
        if (usuarios.existsByCorreo(usuario.getCorreo()))
            throw new IllegalArgumentException("El correo ya está registrado.");
        usuario.setRol("EMP");
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarios.save(usuario);
        empresa.setUsuario(usuarioGuardado);
        empresa.setAprobada(false);
        empresas.save(empresa);
    }

    public void registrarOferente(Usuario usuario, Oferente oferente) {
        if (usuarios.existsByCorreo(usuario.getCorreo()))
            throw new IllegalArgumentException("El correo ya está registrado.");
        usuario.setRol("OFE");
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        usuario.setActivo(true);
        Usuario usuarioGuardado = usuarios.save(usuario);
        oferente.setUsuario(usuarioGuardado);
        oferente.setAprobado(false);
        oferentes.save(oferente);
    }

    // ================================================================
    // EMPRESA
    // ================================================================

    public Empresa empresaByUsuario(Usuario usuario) {
        return empresas.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
    }

    // ================================================================
    // OFERENTE
    // ================================================================

    public Oferente oferenteByUsuario(Usuario usuario) {
        return oferentes.findByUsuario(usuario)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado."));
    }

    public Oferente oferenteById(Long id) {
        return oferentes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado."));
    }

    public void guardarOferente(Oferente oferente) {
        oferentes.save(oferente);
    }

    // ================================================================
    // HABILIDADES DEL OFERENTE
    // ================================================================

    public List<OferenteHabilidad> habilidadesByOferente(Long oferenteId) {
        return habilidades.findByOferenteId(oferenteId);
    }

    @Transactional
    public void guardarHabilidades(Oferente oferente, List<OferenteHabilidad> lista) {
        habilidades.deleteByOferenteId(oferente.getId());
        for (OferenteHabilidad h : lista) {
            h.setOferente(oferente);
        }
        if (!lista.isEmpty()) {
            habilidades.saveAll(lista);
        }
    }

    // ================================================================
    // CARACTERÍSTICAS  (administradas por el admin)
    // ================================================================

    /** Solo nodos raíz (sin padre) */
    public List<Caracteristica> caracteristicasRaiz() {
        return caracteristicas.findByPadreIsNull();
    }

    /**
     * Solo hojas (nodos con padre) — son las que se asignan a puestos y oferentes.
     * Con múltiples niveles, una hoja es cualquier nodo que NO tiene hijos.
     */
    public List<Caracteristica> caracteristicasHojas() {
        // Filtramos: nodos cuya lista de hijos esté vacía
        return caracteristicas.findAll().stream()
                .filter(c -> c.getHijos().isEmpty())
                .toList();
    }

    /** Todos los nodos — para poder elegir cualquiera como padre al crear/editar */
    public List<Caracteristica> todasLasCaracteristicas() {
        return caracteristicas.findAll();
    }

    public List<Caracteristica> hijosDe(Long padreId) {
        return caracteristicas.findByPadreId(padreId);
    }

    public Caracteristica caracteristicaById(Long id) {
        return caracteristicas.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Característica no encontrada."));
    }

    public void guardarCaracteristica(Caracteristica caracteristica) {
        caracteristicas.save(caracteristica);
    }

    public void eliminarCaracteristica(Long id) {
        caracteristicas.deleteById(id);
    }

    // ================================================================
    // PUESTOS
    // ================================================================

    public List<Puesto> puestosRecientes() {
        return puestos.findTop5ByTipoPublicacionAndActivoTrueOrderByFechaRegistroDesc("PUB");
    }

    public List<Puesto> buscarPuestosPublicos(String texto) {
        return puestos.buscarPublicosPorDescripcion(texto == null ? "" : texto.trim());
    }

    public List<Puesto> buscarTodosPuestos(String texto) {
        return puestos.buscarTodosPorDescripcion(texto == null ? "" : texto);
    }

    public List<Puesto> puestosActivosByEmpresa(Empresa empresa) {
        return puestos.findByEmpresaAndActivoTrue(empresa);
    }

    public List<Puesto> todosPuestosByEmpresa(Empresa empresa) {
        return puestos.findByEmpresa(empresa);
    }

    public Puesto puestoById(Long id) {
        return puestos.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado."));
    }

    public void guardarPuesto(Puesto puesto) {
        puestos.save(puesto);
    }

    public void desactivarPuesto(Long id) {
        Puesto puesto = puestoById(id);
        puesto.setActivo(false);
        puestos.save(puesto);
    }

    // ================================================================
    // CARACTERÍSTICAS DE UN PUESTO
    // ================================================================

    public List<PuestoCaracteristica> caracteristicasByPuesto(Long puestoId) {
        return puestosCaracteristicas.findByPuestoId(puestoId);
    }

    public void guardarCaracteristicasPuesto(Puesto puesto, List<PuestoCaracteristica> lista) {
        puestosCaracteristicas.deleteByPuestoId(puesto.getId());
        for (PuestoCaracteristica pc : lista) {
            pc.setPuesto(puesto);
        }
        puestosCaracteristicas.saveAll(lista);
    }

    // ================================================================
    // BÚSQUEDA DE CANDIDATOS
    // ================================================================

    public List<Object[]> buscarCandidatos(Long puestoId, long minCoincidencias) {
        return habilidades.buscarCandidatosPorPuesto(puestoId, minCoincidencias);
    }

    // ================================================================
    // ADMINISTRADOR  –  aprobaciones
    // ================================================================

    public List<Empresa> empresasPendientes() {
        return empresas.findByAprobadaFalse();
    }

    public List<Empresa> empresasAprobadas() {
        return empresas.findByAprobadaTrue();
    }

    public void aprobarEmpresa(Long empresaId) {
        Empresa empresa = empresas.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
        empresa.setAprobada(true);
        empresas.save(empresa);
    }

    public List<Oferente> oferentesPendientes() {
        return oferentes.findByAprobadoFalse();
    }

    public List<Oferente> oferentesAprobados() {
        return oferentes.findByAprobadoTrue();
    }

    public void aprobarOferente(Long oferenteId) {
        Oferente oferente = oferentes.findById(oferenteId)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado."));
        oferente.setAprobado(true);
        oferentes.save(oferente);
    }

    public boolean yaAplico(Long puestoId, Long oferenteId) {
        return aplicaciones.existsByPuestoIdAndOferenteId(puestoId, oferenteId);
    }

    public Aplicacion aplicacionById(Long id) {
        return aplicaciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aplicación no encontrada."));
    }

    // ================================================================
    // REPORTE  –  puestos por mes (para PDF del admin)
    // ================================================================

    public List<Object[]> reportePuestosPorMes() {
        return puestos.contarPuestosPorMes();
    }
    /**
     * Búsqueda pública por características, ordenada por coincidencias.
     *
     * Cada elemento del resultado es un Object[]:
     *   [0] → Puesto
     *   [1] → long  (cantidad de características coincidentes)
     *
     * Si la lista es nula o vacía devuelve lista vacía sin ejecutar la query.
     */
    public List<Object[]> buscarPuestosPublicosPorCaracteristicas(List<Long> caracIds) {
        if (caracIds == null || caracIds.isEmpty()) return List.of();
        return puestos.buscarPublicosPorCaracteristicas(caracIds);
    }
}
