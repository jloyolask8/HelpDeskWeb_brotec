HelpDeskWeb_brotec
==================
## Installation (git download, compile and deploy)

Multitenant branches:

- https://github.com/jloyolask8/HelpDesk_Persistence_Brotec/tree/multitenant_persistence
- https://github.com/jloyolask8/HelpDeskWeb_brotec/tree/multitenant-godesk-testing-merge

email doesnt need multinenant version.

1.$ ssh www.godesk.cl -l jonathan
2.$ cd /home/glassfish/deploy/github-sources/
3. Compilation Order:
drwxr-xr-x. 5 jonathan root 4096 Feb  3 15:23 Email
drwxr-xr-x. 5 jonathan root 4096 Feb  9 13:18 HelpDesk_Persistence_Brotec
drwxr-xr-x  5 jonathan root 4096 Feb  9 13:19 HelpDeskWeb_brotec


4. $ cd Email, $ git status, $ git pull, $ mvn clean install

5.

$ cd HelpDesk_Persistence_Brotec
$ git status #check where in what branch you are
$ git checkout multitenant_persistence  (if you are already in this branch go ahead and use pull)
$ git pull (if you have not downloaded the branches yet use $ git fetch )
$ mvn -Pgodesk_prod clean install

6.

$ cd HelpDeskWeb_brotec
$ git status #check where in what branch you are
$ git checkout multitenant-godesk-testing-merge  #(if you are already in this branch use pull)
$ git pull #(if you have not downloaded the branches yet use $ git fetch )
$ mvn -Pgodesk_prod clean install

if all success you will get a war file inside the server files
deploy it directly

context name of the multitenant deployment is "go" 


## Possible install errors

error: Your local changes to the following files would be overwritten by checkout:
commit 

$ git commit -m 'message'

# or reset all changes 

$ git reset --hard HEAD

# buuum



## Pedientes Godesk

- seria interesante poder enviar mensajes internos a todos los usuarios del sistema.

- desplegar fecha y hora de adjunto. add field fechaCreacion to Attachment.ok done

- no se pq al editar un canal de email no me salen las opciones de debug, etc.
- quitar palabras HISTORIA DEL CASO de la historia del caso.
- notificacion al cliente deben ser publicas por defecto!! - 
ya qie SendMailJob se usa para todos los emails. add public param to sendmailjob

- Cuando se descarta el borrador, no guarda el descarte, se debe

- problemas con las sessiones de chat.

- implementar checks tipo whatsapp para mostrar un email que esta en proceso de envio y cuando esta enviado dos check.
solo para minimizar espacio.

deploy jwatch
http://code.google.com/p/jwatch/wiki/Installation
remove all links to see jobs inside the app.

MANIWIS DEBE PROVEER UN DOC CON LOS TERMINOS DE NUSTRO SERVICIO. LUEGO ATACHAR/insertar ESE DOC A LA PAGINA REGISTRARSE.

embeddedFromNewTicketController  - newTicketEmbedded.xhtml NOT WORKING
WE TO MAKE ACCESS TO ALL DATA IN A MULTITENANT WAY, IT STILL ACCESS DATA OLD WAY.
THE IDEA HERE IS NOT TO USE ANY OTHER BEANCONTROLLER, INSTEAD CREATE ALL NEEDED METHOS TO GET DATA INSIDE 
embeddedFromNewTicketController, THE TENANT ID IS PASSED AS A PARAMETER TO THIS BEAN AND IT GETS DATA USING PARENTS UTILITIES.



en convinar casos, crear un nuevo caso. y convinar todo en el nuevo caso. cerrar los convinados.
y permitir que esta operacion se pueda deshacer. se elimina el nuevo caso convinado y se reabren los casos cerrados.
ojo que falta convinar los emails de clientes, cc, etc.

separar asunto de list casos, para ordernar.

- convinar casos como un boton en el index. quitar desde dentro del Edit caso.

- convinar casos grupales.

- notas privadas solo visibles por el autor no otros usuarios.

- casos colaborativos mejorar e implementar de una forma simplificada como tareas. y arbol.

mejorar manejo contactos,
Organizacion -> Area -> Sucursal -> Datos de contacto.

- separar tours, 1. dashboard, 2. inbox. 3. settings.


en editar area agregar link directo a crear el canal necesario para la comunicación.

- disable tour debug on production.

- disable cache selectively. enable for casos. check cache problems.

- setting de nombre de empresa: asignarle el valor ingresado en el register.

- explicar la creacion de eventos publicos y mostrar formulario de inscripcion + form new casos embedded.
hacer una pagina de test.

- add button save all in SLA Settings.
- remove the users creator and add a invite users by email, send email and create them an account.

- mejorar el mail con historia, ver "slack" or zendesk ...

- crear un caso de prueba cuando se crea la cuenta.

Graficar el customer satisfaction en el dashboard.

- Remover historia del area de texto responder.godesk done. pending betterlife/beltec/brotec.
- facilitar la creacion de clientes contactos, agregar tabla organizacion/empresa.
- Implementar drag and drop en Direcciones de correos - respuestas por correos.
http://jqueryui.com/autocomplete/#multiple-remote
http://goodies.pixabay.com/jquery/tag-editor/demo.html
http://brianreavis.github.io/selectize.js/

- add more dashboards
- add get started page with instructions on how to proceed on the setup.

Ejemplo:

+Configure your support emails (Skip)
Configure emails to start receiving cases.

+Add UserVoice to your website (Skip)
Definitely setup your portal, but our embedded widgets are what make UserVoice awesome. With them installed you'll not only give your users a way to contact you when there's a problem, but you'll drive self-service of those issues with Instant Answers and also collect in-depth product feedback & customer satisfaction data.
Next: Use the widget builder to customize your widget and then copy the code into your HTML. (Skip)

+Add Custom Fields (Skip)
Need to categorize your tickets by more than just "open" or "closed"? Add a custom fields to ask additional questions of customers, agents or setup metadata fields you can populate via Javascript.

+Double-check who's responsible (Skip)
Currently all your tickets will be assigned to jonathan. Change who tickets are assigned to by editing or adding Rules in Ticket Settings.

+Add Ticket Rules (Skip)
Rules allow you to define actions (like auto reply, or assignment) to take on new tickets that match a specific set of criteria.

+Upload your logo and customize colors (Skip)
Your site is looking pretty drab. Cheer it up by editing your site design to make it feel more like your company.



- implementar logout countdown cuando se detecte inactividad.
- foto de perfil de clientes y agentes =)
- +Inbox FiltroVista by default  . ok - falta test
- mostrar tags como Vistas in tabs de vistas

<h:panelGroup layout="block" styleClass="panel panel-transparent hidden-xs" 
                                  rendered="#{(not empty tagCloudBean.etiquetasByUsuario) and (not casoController.mobileClient)}">
                        <div class="panel-heading">
                            <span class="panel-title"><i class="panel-title-icon fa fa-tags"></i>Mis Tags</span>
                        </div>

                        <div class="panel-body">

                            <ui:repeat value="#{tagCloudBean.etiquetasByUsuario}" var="etiquetaUser">
                                <p:commandLink actionListener="#{tagCloudBean.onEtiquetaSelected(etiquetaUser.tagId)}"
                                               oncomplete="$('.nav-pills, .nav-tabs').tabdrop();"
                                               styleClass="label label-tag label-info" value="#{etiquetaUser}"
                                               update=":vistasList:vistasForm :mainform:messages :form0 :formfilter:panelFilter0:panelFilter">

                                </p:commandLink>&nbsp;
                            </ui:repeat>


                        </div>
                    </h:panelGroup>


- Enter in your credit card information to continue your service beyond the free trial

- crear un modelo de datos "public" en el cual manejar la informacion de todas las cuentas creadas.
una sola tabla es suficiente.






