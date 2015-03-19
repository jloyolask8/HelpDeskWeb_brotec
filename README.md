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


4. $ cd Email, $ git pull, $ mvn clean install

5.

$ cd HelpDesk_Persistence_Brotec
$ git checkout multitenant_persistence  (if you are already in this branch use pull)
$ git pull (if you have not downloaded the branches yet use $ git fetch )
$ mvn -Pgodesk_prod clean install

5.

$ cd HelpDeskWeb_brotec
$ git checkout multitenant-godesk-testing-merge  (if you are already in this branch use pull)
$ git pull (if you have not downloaded the branches yet use $ git fetch )
$ mvn -Pgodesk_prod clean install

if all success you will get a war file inside the server files
deploy it directly

context name of our multitenant is "go" 


## Possible install errors

error: Your local changes to the following files would be overwritten by checkout:
commit or reset changes 
$ git commit -m 'message'
or $ git reset --hard HEAD
bum



## Pedientes Godesk

- guardar caso no hacer redirect sino update.
- concurrent problem at auditLogController.getActivityLogs(
- descripcion del caso no debe ser editable con inline. ya que afecta al [+] ademas agregar prettyDate al escribió...
- notificacion al cliente deben ser publicas por defecto!!
- Cuando se descarta el borrador, no guarda el descarte, se debe
- el chat sube con la pagina.
- problemas con las sessiones de chat.
- desplegar fecha y hora de adjunto.

deploy jwatch
http://code.google.com/p/jwatch/wiki/Installation
remove all links to see jobs inside the app.

MANIWIS DEBE PROVEER UN DOC CON LOS TERMINOS DE NUSTRO SERVICIO. LUEGO ATACHAR/insertar ESE DOC A LA PAGINA REGISTRARSE.

embeddedFromNewTicketController  - newTicketEmbedded.xhtml NOT WORKING
WE TO MAKE ACCESS TO ALL DATA IN A MULTITENANT WAY, IT STILL ACCESS DATA OLD WAY.
THE IDEA HERE IS NOT TO USE ANY OTHER BEANCONTROLLER, INSTEAD CREATE ALL NEEDED METHOS TO GET DATA INSIDE 
embeddedFromNewTicketController, THE TENANT ID IS PASSED AS A PARAMETER TO THIS BEAN AND IT GETS DATA USING PARENTS UTILITIES.

no se pq al editar un canal de email no me salen las opciones de debug, etc.

en convinar casos, crear un nuevo caso. y convinar todo en el nuevo caso. cerrar los convinados.
y permitir que esta operacion deshacer. se elimina el nuevo caso convinado y se reabren los casos cerrados.
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

- crear caso de prueba cuando se crea la cuenta.

Graficar el customer satisfaction en el dashboard.

- Remover historia del area de texto responder.godesk done. pending betterlife/beltec/brotec.
- facilitar la creacion de clientes contactos, agregar tabla organizacion.
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
- +Inbox FiltroVista by default  
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


ERROR CUACULA: Probablemente problema del Cache Policy =(
=============


javax.el.ELException: /script/template_inbox_bootstrap.xhtml @112,93 listener="#{casoController.initializeData}": java.util.ConcurrentModificationException

Caused by: java.util.ConcurrentModificationException
	at java.util.HashMap$HashIterator.nextEntry(HashMap.java:922)
	at java.util.HashMap$KeyIterator.next(HashMap.java:956)
	at org.eclipse.persistence.descriptors.ClassDescriptor.notifyReferencingDescriptorsOfIsolation(ClassDescriptor.java:3914)
	at org.eclipse.persistence.descriptors.CachePolicy.postInitialize(CachePolicy.java:177)
	at org.eclipse.persistence.descriptors.ClassDescriptor.postInitialize(ClassDescriptor.java:3902)
	at org.eclipse.persistence.internal.sessions.AbstractSession.updateTablePerTenantDescriptors(AbstractSession.java:1390)
	at org.eclipse.persistence.sessions.server.ClientSession.<init>(ClientSession.java:136)
	at org.eclipse.persistence.sessions.server.ServerSession.acquireClientSession(ServerSession.java:394)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getActivePersistenceContext(EntityManagerImpl.java:1933)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getActiveSession(EntityManagerImpl.java:1232)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getQueryHints(EntityManagerImpl.java:2587)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.find(EntityManagerImpl.java:588)
	at com.itcs.helpdesk.persistence.jpa.AbstractJPAController.createPredicate(AbstractJPAController.java:396)
	at com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade.countEntities(JPAServiceFacade.java:638)
	at com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade.countEntities(JPAServiceFacade.java:620)
	at com.itcs.helpdesk.jsfcontrollers.CasoController.initializeData(CasoController.java:1351)



2
=
[#|2015-02-25T19:15:19.979-0300|SEVERE|oracle-glassfish3.1.2|com.itcs.helpdesk.quartz.DownloadEmailJob|_ThreadID=325;_ThreadName=Thread-2;|error on execute DownloadEmailJob. Canal:contacto@godesk.cl
java.util.ConcurrentModificationException
	at java.util.HashMap$HashIterator.nextEntry(HashMap.java:922)
	at java.util.HashMap$KeyIterator.next(HashMap.java:956)
	at org.eclipse.persistence.descriptors.ClassDescriptor.notifyReferencingDescriptorsOfIsolation(ClassDescriptor.java:3914)
	at org.eclipse.persistence.descriptors.CachePolicy.postInitialize(CachePolicy.java:177)
	at org.eclipse.persistence.descriptors.ClassDescriptor.postInitialize(ClassDescriptor.java:3902)
	at org.eclipse.persistence.internal.sessions.AbstractSession.updateTablePerTenantDescriptors(AbstractSession.java:1390)
	at org.eclipse.persistence.sessions.server.ClientSession.<init>(ClientSession.java:136)
	at org.eclipse.persistence.sessions.server.ServerSession.acquireClientSession(ServerSession.java:394)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getActivePersistenceContext(EntityManagerImpl.java:1933)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getActiveSession(EntityManagerImpl.java:1232)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.getQueryHints(EntityManagerImpl.java:2587)
	at org.eclipse.persistence.internal.jpa.EntityManagerImpl.find(EntityManagerImpl.java:588)
	at com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade.find(JPAServiceFacade.java:390)
	at com.itcs.helpdesk.persistence.jpa.service.JPAServiceFacade.find(JPAServiceFacade.java:369)
	at com.itcs.helpdesk.quartz.DownloadEmailJob.revisarCorreo(DownloadEmailJob.java:113)
	at com.itcs.helpdesk.quartz.DownloadEmailJob.execute(DownloadEmailJob.java:65)
	at org.quartz.core.JobRunShell.run(JobRunShell.java:202)
	at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:573)
|#]

Open/Future items
==================
    Bug 355458: Provide admin user access data from multiple tenants

BUG for sequences numbers
=========================

    Tenant column when part of the entity identifier
        Incorporate sequence generators



