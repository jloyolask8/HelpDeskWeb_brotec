package com.itcs.helpdesk.webservices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jorge
 */
public class DatosCaso {

    private String idArea;//id del producto
    private String idPrioridad;//id del producto
    private String producto;//id del producto
    private String modelo;//id del modelo
    private String nombre;//client
    private String apellidos;//client
    private String rut;//client
    private String telefono;//client
    private String telefono2;//client
    private String email;//client
//    private String ciudad;//client
    private String comuna;//client
    private String asunto;//caso
    private String descripcion;//caso
    private String tipoCaso;//id del tipo caso
    private List<String> tags;
    private List<DatosCaso.CustomField> customFields;
    
    //brotec-icafal specifics
    private boolean credito = false;
    private Date fechaEstimadaCompra;

    public DatosCaso() {
        customFields = new ArrayList<DatosCaso.CustomField>();
    }

    public DatosCaso(String producto, String nombre, String apellidos, String rut, String telefono, String telefono2, String email, String comuna, String descripcion, String tipoCaso) {
        this.producto = producto;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rut = rut;
        this.telefono = telefono;
        this.telefono2 = telefono2;
        this.email = email;
        this.comuna = comuna;
        this.descripcion = descripcion;
        this.tipoCaso = tipoCaso;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DatosCaso [");
        builder.append("apellidos=");
        builder.append(apellidos);
        builder.append(", comuna=");
        builder.append(comuna);
        builder.append(", descripcion=");
        builder.append(descripcion);
        builder.append(", email=");
        builder.append(email);
        builder.append(", modelo=");
        builder.append(modelo);
        builder.append(", nombre=");
        builder.append(nombre);
        builder.append(", producto=");
        builder.append(producto);
        builder.append(", rut=");
        builder.append(rut);
        builder.append(", telefono=");
        builder.append(telefono);
        builder.append(", telefono2=");
        builder.append(telefono2);
        builder.append(", tipoCaso=");
        builder.append(tipoCaso);
        builder.append("]");
        return builder.toString();
    }

    public void parseNombre(String nombresString) {

        String partes[] = nombresString.split(" ");
        if (partes != null) {
            try {
                if (partes != null && partes.length == 1) {
                    this.setNombre(partes[0]);
                } else if (partes != null && partes.length == 2) {
                    this.setNombre(partes[0]);
                    this.setApellidos(partes[1]);
                } else if (partes != null && partes.length == 3) {
                    this.setNombre(partes[0]);
                    this.setApellidos(partes[1] + " " + partes[2]);
                } else if (partes != null && partes.length == 4) {
                    this.setNombre(partes[0] + " " + partes[1]);
                    this.setApellidos(partes[2] + " " + partes[3]);
                } else if (partes != null && partes.length == 5) {
                    this.setNombre(partes[0] + " " + partes[1] + " " + partes[2]);
                    this.setApellidos(partes[3] + " " + partes[4]);
                } else if (partes != null && partes.length == 6) {
                    this.setNombre(partes[0] + " " + partes[1] + " " + partes[2]);
                    this.setApellidos(partes[3] + " " + partes[4] + " " + partes[5]);
                } else {
                    this.setNombre(nombresString);
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.setNombre(nombresString);
            }
        }

    }

    /**
     * @return the rut
     */
    public String getRut() {
        return rut;
    }

    /**
     * @param rut the rut to set
     */
    public void setRut(String rut) {
        this.rut = rut;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @param descripcion the descripcion to set
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * @return the producto
     */
    public String getProducto() {
        return producto;
    }

    /**
     * @param producto the producto to set
     */
    public void setProducto(String producto) {
        this.producto = producto;
    }

    /**
     * @return the apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * @param apellidos the apellidos to set
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * @return the telefono2
     */
    public String getTelefono2() {
        return telefono2;
    }

    /**
     * @param telefono2 the telefono2 to set
     */
    public void setTelefono2(String telefono2) {
        this.telefono2 = telefono2;
    }

    /**
     * @return the ciudad
     */
//    public String getCiudad() {
//        return ciudad;
//    }
//
//    /**
//     * @param ciudad the ciudad to set
//     */
//    public void setCiudad(String ciudad) {
//        this.ciudad = ciudad;
//    }
    /**
     * @return the tipoCaso
     */
    public String getTipoCaso() {
        return tipoCaso;
    }

    /**
     * @param tipoCaso the tipoCaso to set
     */
    public void setTipoCaso(String tipoCaso) {
        this.tipoCaso = tipoCaso;
    }

    /**
     * @return the comuna
     */
    public String getComuna() {
        return comuna;
    }

    /**
     * @param comuna the comuna to set
     */
    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    /**
     * @return the modelo
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * @param modelo the modelo to set
     */
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    /**
     * @return the idArea
     */
    public String getIdArea() {
        return idArea;
    }

    /**
     * @param idArea the idArea to set
     */
    public void setIdArea(String idArea) {
        this.idArea = idArea;
    }

    /**
     * @return the idPrioridad
     */
    public String getIdPrioridad() {
        return idPrioridad;
    }

    /**
     * @param idPrioridad the idPrioridad to set
     */
    public void setIdPrioridad(String idPrioridad) {
        this.idPrioridad = idPrioridad;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<String>();
        }
        if (tag != null) {
            tags.add(tag.trim());
        }

    }

    public void addCustomFieldValue(String key, String val) {
        
        if(key == null || val == null){
            return;
        }

        if (customFields != null) {
            customFields.add(new DatosCaso.CustomField(key.trim(), val.trim()));
        }

    }

    /**
     * @return the customFields
     */
    public List<DatosCaso.CustomField> getCustomFields() {
        return customFields;
    }

    /**
     * @param customFields the customFields to set
     */
    public void setCustomFields(List<DatosCaso.CustomField> customFields) {
        this.customFields = customFields;
    }

    /**
     * @return the asunto
     */
    public String getAsunto() {
        return asunto;
    }

    /**
     * @param asunto the asunto to set
     */
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    /**
     * @return the credito
     */
    public boolean isCredito() {
        return credito;
    }

    /**
     * @param credito the credito to set
     */
    public void setCredito(boolean credito) {
        this.credito = credito;
    }

    /**
     * @return the fechaEstimadaCompra
     */
    public Date getFechaEstimadaCompra() {
        return fechaEstimadaCompra;
    }

    /**
     * @param fechaEstimadaCompra the fechaEstimadaCompra to set
     */
    public void setFechaEstimadaCompra(Date fechaEstimadaCompra) {
        this.fechaEstimadaCompra = fechaEstimadaCompra;
    }

    public static class CustomField {

        private String fieldKey;
        private String fieldValue;

        public CustomField(String fieldKey, String fieldValue) {
            this.fieldKey = fieldKey;
            this.fieldValue = fieldValue;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.fieldKey != null ? this.fieldKey.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DatosCaso.CustomField other = (DatosCaso.CustomField) obj;
            if ((this.fieldKey == null) ? (other.fieldKey != null) : !this.fieldKey.equals(other.fieldKey)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CustomField [");
            builder.append("fieldKey=");
            builder.append(fieldKey);
            builder.append(", fieldValue=");
            builder.append(fieldValue);
            builder.append("]");
            return builder.toString();
        }

        /**
         * @return the fieldKey
         */
        public String getFieldKey() {
            return fieldKey;
        }

        /**
         * @param fieldKey the fieldKey to set
         */
        public void setFieldKey(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        /**
         * @return the fieldValue
         */
        public String getFieldValue() {
            return fieldValue;
        }

        /**
         * @param fieldValue the fieldValue to set
         */
        public void setFieldValue(String fieldValue) {
            this.fieldValue = fieldValue;
        }
    }
}
