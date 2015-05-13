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

https://trello.com/b/JYJmz3pZ/new-features