REM $Id$ MER00027.sql,v 1.7 04/07/2014 00:00:00 QDT Exp $

/*****************************************************************
****                                                          ****
****                     PACKAGE TEMPLATE                     ****
****                                                          ****
******************************************************************

SCRIPT NAME      : MER00027.sql   

AUTHOR           : Marco Francardi (0354232180)   

RESPONSIBLE      : Gianpietro Sassi (0354232128)

SG/ST            : SG06881 
  
BUG/OTHER        : 
  
SYSTEM           : Merlino

MODULE           : 

VERSION          : 1.7 - 04/07/2014 Ora non vengono piu' recuperati i dati relativi alla applicazione CheckNbaExistance perche' inutili
                   1.6 - 05/12/2013 Aggiunta commit tra delete e insert
				           1.5 - 15/11/2013 Aggiunta delete preventiva
				           1.4 - 09/10/2013 Aggiunto controllo 'ms for' su query
				           1.3 - 23/09/2013 Aggiunto attributo CONTEXT a tavola USAGE_STATS_SEARCH_DETAIL
				           1.2 - 13/09/2013 Modificati nomi attributi tavole USAGE_STATISTICS e USAGE_STATS_SEARCH_DETAIL
				           1.1 - 09/09/2013 Aggiunti commenti e migliorate performance con ottimizzazione query e inserimento hint
				           1.0 - 30/08/2013

DESCRIPTION      : Compilazione package PKG_USAGE_STATISTICS

CONSTRAINT       :

WARNING          :

DATABASE         : QDT

SCHEMA           : NTGCUSER
         
