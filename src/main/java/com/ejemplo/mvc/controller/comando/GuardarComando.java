package com.ejemplo.mvc.controller.comando;

import com.ejemplo.mvc.model.Tarea;
import com.ejemplo.mvc.service.TareaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GuardarComando implements Comando {
    private static final List<String> PRIORIDADES = List.of("Alta", "Media", "Baja");

    private final TareaService service;

    public GuardarComando(TareaService service) { this.service = service; }

    @Override
    public String ejecutar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String titulo = req.getParameter("titulo");
        String categoria = req.getParameter("categoria");
        String prioridad = req.getParameter("prioridad");
        String fechaStr = req.getParameter("fechaLimite");

        int maxLongitud = (Integer)
                req.getServletContext().getAttribute("maxLongitudTitulo");

        Map<String, String> errores = new LinkedHashMap<>();

        if (titulo == null || titulo.trim().isEmpty()) {
            errores.put("titulo", "El título es obligatorio.");
        } else if (titulo.trim().length() > maxLongitud) {
            errores.put("titulo",
                    "El título no debe superar " + maxLongitud + " caracteres.");
        }

        if (categoria == null || categoria.trim().isEmpty()) {
            errores.put("categoria", "La categoría es obligatoria.");
        }

        if (!PRIORIDADES.contains(prioridad)) {
            errores.put("prioridad", "Seleccione una prioridad válida.");
        }

        Date fechaLimite = parsearFecha(fechaStr);
        if (fechaLimite == null) {
            errores.put("fechaLimite", "Use el formato yyyy-MM-dd (ej: 2026-12-15).");
        } else if (fechaLimite.before(inicioDeHoy())) {
            errores.put("fechaLimite", "La fecha límite no puede estar en el pasado.");
        }

        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            req.setAttribute("titulo", titulo);
            req.setAttribute("categoria", categoria);
            req.setAttribute("prioridad", prioridad);
            req.setAttribute("fechaLimite", fechaStr);
            return "/WEB-INF/views/formulario.jsp";
        }

        service.guardar(new Tarea(0, titulo.trim(), categoria.trim(),
                prioridad, fechaLimite));
        resp.sendRedirect(req.getContextPath() + "/app?comando=listar");
        return null;
    }

    private Date parsearFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            formato.setLenient(false);
            return formato.parse(texto.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private Date inicioDeHoy() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
