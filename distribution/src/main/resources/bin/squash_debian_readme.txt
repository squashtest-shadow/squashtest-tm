# squash_debian_readme.txt : Installation de l'environnement du script de lancement de squash

1) Les livrables :
   - squash_default : Fichier d'initialisation de variables d'environnement utiles à squash.
		En étant connecté : root
		- A placer dans le répertoire système /etc/default.
		- A renommer en squash (mv squash_default squash).
		- Contient la variable SQUASH_HOME qui doit être obligatoirement initialisée avec le chemin
		  où sera installée l'application : squash.
		  Ex: SQUASH_HOME=/home/johndoe/squashtest-csp-0.20.1 (au dessus de ./bin)
		- Contient également certaines variables (en commentaire), définies par défaut dans
		  le script shell de lancement de squash, mais qui peuvent être surchargées si nécessaire.
		  Ex: HTTP_PORT = 2603 (sans le # devant) 
			Pour surcharger la valeur 8080 définie par défaut dans le script shell de lancement de squash.
		  Ex: JAVA_ARGS = "-Xmx512m"
			Pour rajouter des arguments supplémentaires au lancement de la JVM (seul l'argument -server est passé par défaut).

   - squash_debian : Script shell de lancement de squash
		A placer dans le répertoire $SQUASH_HOME/bin.
		A renommer en squash (mv squash_debian squash).
		Donner les droits d'exécution si nécessaire au script shell : squash
		Ex: chmod +x squash

2) Les fichiers à supprimer :
	(org.apache.felix.gogo.command-0.6.1.jar, org.apache.felix.gogo.runtime-0.6.1.jar, org.apache.felix.gogo.shell-0.6.1.jar) :
		Ces fichiers permettant la gestion de la console OSGI "gogo", seront à supprimer du répertoire bundles/ du package de livraison de squash.

3) Démarrage automatique du service : squash
	En étant connecté : root
	- Dans le répertoire /etc/init.d, créer un lien symbolique "squash" pointant sur l'emplacement physique
	  du script shell de lancement de squash.
	  Ex:Si SQUASH_HOME=/home/johndoe/squashtest-tm-1.0.0.RELEASE (définie dans /etc/default/squash)
	  ln -s /home/johndoe/squashtest-tm-1.0.0.RELEASE/bin/squash
	- Pour démarrer le service squash au lancement de la machine :
	  update_rc.d squash defaults

4) Commande de gestion du service : squash
	En étant connecté : root
	- /etc/init.d/squash status  : Pour consulter l'état du service squash.
	- /etc/init.d/squash start   : Pour démarrer le service squash.
	- /etc/init.d/squash stop    : Pour arrêter le service squash.
	- /etc/init.d/squash restart : Pour arrêter puis redémarrer le service squash.
	
