package com.itcs.helpdesk.webservices;

/**
 *
 * @author jorge
 */
public class WSResponse 
{
    private String mensaje;
    private String codigo;

    /**
     * Mensaje relacionado con el fallo en la creacion de un caso.
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @param mensaje the mensaje to set
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * Codigo 0 ingreso fallido.
     * Codigo 1 ingreso exitoso.
     * @return the codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
