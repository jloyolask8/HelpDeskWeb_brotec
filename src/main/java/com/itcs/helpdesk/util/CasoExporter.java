/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.util;

import com.itcs.helpdesk.persistence.entities.Caso;
import com.itcs.helpdesk.persistence.entities.Nota;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author jorge
 */
public class CasoExporter {

    public CasoExporter() {
    }

    public static String exportToHtmlText(Caso caso) {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("<h1>Detalle del caso</h1><br/>");
        sbuilder.append(caso.getTipoCaso().getNombre());
        sbuilder.append(" #");
        sbuilder.append(caso.getIdCaso());
        sbuilder.append(": ");
        sbuilder.append(caso.getTema());

        if (caso.getEmailCliente() != null) {
            sbuilder.append("<br/><br/><b>Email cliente:</b> ");
            sbuilder.append(caso.getEmailCliente().getEmailCliente());

            if (caso.getEmailCliente().getCliente() != null) {
                sbuilder.append("<br/><b>Nombre Cliente:</b> ");
                sbuilder.append(caso.getEmailCliente().getCliente().getCapitalName());

                if (caso.getEmailCliente().getCliente().getFono1() != null) {
                    sbuilder.append("<br/><b>Teléfono Cliente:</b> ");
                    sbuilder.append(caso.getEmailCliente().getCliente().getFono1());

                }
            }

        }

        sbuilder.append("<br/><br/><b>Agente asignado:</b> ");
        sbuilder.append(caso.getOwner() != null ? caso.getOwner().getCapitalName() : "No está asignado.");
        sbuilder.append("<br/><b>Fecha de creación:</b> ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sbuilder.append(caso.getFechaCreacion() != null ? sdf.format(caso.getFechaCreacion()) : "-");
        sbuilder.append("<br/><b>Canal:</b> ");
        sbuilder.append(caso.getIdCanal() != null ? caso.getIdCanal().getNombre() : "-");
        sbuilder.append("<br/><b>Proyecto:</b> ");
        sbuilder.append(caso.getIdProducto() != null ? caso.getIdProducto().getNombre() : "-");
        sbuilder.append("<br/><b>Mensaje/Descripción:</b> <br/>");
        sbuilder.append(caso.getDescripcion());

        try {
            sbuilder.append("<br/><h2>").append(caso.getNotaList().size()).append(" Actividades:</h2> <br/>");
        } catch (Exception e) {
            //ignore
            sbuilder.append("<br/><h2>Actividades:</h2> <br/>");
        }

        if (caso.getNotaList() != null) {
            if (caso.getNotaList().size() > 0) {
                for (Nota nota : caso.getNotaList()) {
                    sbuilder.append("<hr/>");
                    sbuilder.append("<br/><b>Fecha:</b> ");
                    sbuilder.append(nota.getFechaCreacion());
                    sbuilder.append("<br/><b>Autor:</b> ");
                    sbuilder.append(nota.getCreadaPor() != null ? nota.getCreadaPor().getCapitalName() : "");
                    sbuilder.append(nota.getEnviadoPor() != null ? nota.getEnviadoPor() : "");
                    sbuilder.append("<br/><b>Comentario:</b> <br/>").append(nota.getTexto());
                }
            }
        } else {
            sbuilder.append("El caso no tiene actividades.");
        }

        return sbuilder.toString();
    }
}
