# Sistema Distribuito

Questo progetto implementa un sistema distribuito per l'analisi di immagini e pdf, utilizzando Docker Compose per orchestrare i vari servizi.

## Prerequisiti

- Docker
- Docker Compose

## Avvio del Sistema

Per avviare tutti i servizi, eseguire il seguente comando dalla directory principale del progetto:

```bash
docker compose up -d --build
```

Questo comando:

- Costruirà le immagini Docker necessarie (`--build`)
- Avvierà tutti i servizi in modalità detached (`-d`)
- Configurerà automaticamente la rete e le dipendenze tra i servizi

## Note

- I servizi verranno avviati in background grazie all'opzione `-d`
- Per visualizzare i log dei servizi in esecuzione, è possibile utilizzare `docker compose logs -f`
- Per fermare tutti i servizi, utilizzare `docker compose down`
