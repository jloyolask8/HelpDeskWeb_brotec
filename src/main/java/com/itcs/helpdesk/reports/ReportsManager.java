/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itcs.helpdesk.reports;

import com.itcs.helpdesk.persistence.entities.Caso;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRProperties;

/**
 *
 * @author jorge
 */
public class ReportsManager {

    private static JasperReport jasperActaPreEntregaFile;
    private static JasperReport jasperVisitaPreventivaPostventa;

    public static byte[] createActaPreEntrega(Caso caso, InputStream in) {
        JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", true);
        try {
            if (jasperActaPreEntregaFile == null) {
                URL urlActa = ReportsManager.class.getClassLoader().getResource("ActaPreEntrega.jasper");
                jasperActaPreEntregaFile = (JasperReport) JRLoader.loadObject(urlActa);
            }

            Map parameters = new HashMap();
            parameters.put("casoOrigen", caso);
            parameters.put("logoProyecto", in);
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperActaPreEntregaFile, parameters,
                    new JRBeanCollectionDataSource(caso.getCasosHijosList()));
            return createPdfFromJasper(jasperPrint, "actaPreEntrega"+caso.getIdCaso()+".pdf");
           
            
//            InputStream in = new ByteArrayInputStream(bytes);
            
            
        } catch (Exception ex) {
            Logger.getLogger(ReportsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] createVisitaPreventivaPostventa(Caso caso) {
        JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", true);
        try {
            if (jasperVisitaPreventivaPostventa == null) {
                URL urlActa = ReportsManager.class.getClassLoader().getResource("postventa.jasper");
                jasperVisitaPreventivaPostventa = (JasperReport) JRLoader.loadObject(urlActa);
            }

            Map parameters = new HashMap();
            parameters.put("casoOrigen", caso);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperVisitaPreventivaPostventa, parameters);
            return createPdfFromJasper(jasperPrint, "visitaPreventivaPostventa"+caso.getIdCaso()+".pdf");
        } catch (Exception ex) {
            Logger.getLogger(ReportsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static byte[] createPdfFromJasper(JasperPrint jasperPrint, String filename) throws IOException, JRException
    {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRExporter exporter = new JRPdfExporter();
            //exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "ISO-8859-15");
            exporter.setParameter(JRPdfExporterParameter.METADATA_TITLE, "Acta de pre-entrega");
            exporter.setParameter(JRPdfExporterParameter.METADATA_SUBJECT, "");
            exporter.setParameter(JRPdfExporterParameter.METADATA_KEYWORDS, "Acta brotec-icafal");
            exporter.setParameter(JRPdfExporterParameter.METADATA_CREATOR, "GoDesk");
            exporter.setParameter(JRPdfExporterParameter.METADATA_AUTHOR, "GoDesk");
        //exporter.setParameter(JRPdfExporterParameter.PERMISSIONS,new Integer(PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY) );
            //TODO: ver propiedad para no copiar
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);

            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            exporter.exportReport();
            FileOutputStream output = new FileOutputStream(filename);
            return baos.toByteArray();
    }
}
