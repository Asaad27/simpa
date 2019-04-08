#!/usr/bin/python2
# -*- coding: utf-8 -*-
import json

config=dict()

#NAME
config['name']=raw_input("Veuillez donner un nom au fichier de configuration (l'extention .json sera automatiquement ajoutée) : \n\t")

#URL
config['urls']=["http"+raw_input("Veuillez donner l'adresse racine du site à visiter : \n\thttp")]

#NOFOLLOW
baseUrl=config['urls'][0]
i=baseUrl.index('://')
baseUrl=baseUrl[i+3:].split('/')[0].replace('.','\.')
config['noFollow']=['^javascript:.*', '.*#.*', '^mailto:.*', '^http[s]?:\\/\\/(?!'+baseUrl+').*$']
if (raw_input("Y a-t-il des pages que vous souhaiteriez éviter (ex: delete_user.php) ? [o/N]\n\t").lower()=="o"):
    print "Veuillez fournir un ou plusieurs expressions régulières correspondant aux liens ou forms à ne pas suivre/envoyer"
    paramList=[]
    param=raw_input("\t")
    while param!='':
        paramList+=[param]
        param=raw_input("\t")
    config['noFollow']+=paramList

#COOKIE
if (raw_input("Souhaitez-vous que le cookie conserve une valeur particulière pendant l'exploration ? [o/N]\n\t").lower()=="o"):
    config['cookies']=raw_input("Veuillez donner la valeur du cookie à inclure dans les requêtes :\n\t")

#RESET
if (raw_input("Est-il possible de remettre à zéro l'application par une URL ? [o/N]\n\t").lower()=="o"):
    config['reset']='http'+raw_input("Veuillez indiquer l'adresse de remise-à-zéro :\n\thttp")

#HTTP_auth
if (raw_input("Une authentification HTTP (Basic) est-elle nécessaire ? [o/N]\n\t").lower()=="o"):
    config['basicAuthUser']=raw_input("User : ")
    config['basicAuthPass']=raw_input("Pass : ")

#SELECTOR
if (raw_input("Souhaitez-vous restreindre l'exploration à un certain cadre (selecteur CSS) ? [o/N]\n\t").lower()=="o"):
    config['limitSelector']=raw_input("Veuillez entrer la valeur du selecteur CSS approprié :\n\t")

#DATA
config['data']=dict()
if (raw_input("Connaissez-vous des données qui seraient utiles au crawler (ex: valeur utile à entrer dans un champ de recherche, etc.) ? [o/N]\n\t").lower()=="o"):
    wantMore=True
    while wantMore:
        key=raw_input("Veuillez indiquer le paramètre HTTP concerné :\n\t")
        print "Veuillez fournir une ou plusieurs valeurs à tester pour ce paramètre (séparez les valeurs par des retours à la ligne, validez la liste avec deux retours à la ligne consécutifs)"
        itemsList=[]
        item=raw_input("\t")
        while item!='':
                itemsList+=[item]
                item=raw_input("\t")
        config['data'][key]=itemsList
        wantMore=raw_input("Y a-t-il un autre paramètre à ajouter ? [o/N]\n\t").lower()=="o"

#USELESS PARAMETERS
if (raw_input("Connaissez-vous des paramètres HTTP qui n'ont pas vraiment d'influence sur l'application (ex:'lang') ? [o/N]\n\t").lower()=="o"):
    print "Veuillez fournir un ou plusieurs noms de paramètres dont vous savez l'inutilité (séparez les valeurs par des retours à la ligne, validez la liste avec deux retours à la ligne consécutifs)"
    paramList=[]
    param=raw_input("\t")
    while param!='':
        paramList+=[param]
        param=raw_input("\t")
    config['uselessParameters']=paramList

with open(config["name"]+".json","w") as f:
    f.write(json.dumps(config))
    print "Le fichier de configuration a été écrit dans " + config["name"] + ".json"

