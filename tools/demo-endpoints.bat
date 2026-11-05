@echo off
setlocal
set BASE=http://localhost:8082/banque/comptes
echo =====================================================================
echo  TP 8 - Spring @RestController : demonstration des endpoints
echo  Serveur : http://localhost:8082
echo =====================================================================
echo.

echo ### 1. GET /banque/comptes  (Accept: application/json)
curl.exe -s -i -H "Accept: application/json" %BASE%
echo.
echo.

echo ### 2. GET /banque/comptes  (Accept: application/xml)
curl.exe -s -i -H "Accept: application/xml" %BASE%
echo.
echo.

echo ### 3. GET /banque/comptes/1  (Accept: application/json)
curl.exe -s -i -H "Accept: application/json" %BASE%/1
echo.
echo.

echo ### 4. GET /banque/comptes/1  (Accept: application/xml)
curl.exe -s -i -H "Accept: application/xml" %BASE%/1
echo.
echo.

echo ### 5. GET /banque/comptes/9999  (identifiant inconnu -^> 404)
curl.exe -s -i -H "Accept: application/json" %BASE%/9999
echo.
echo.

echo ### 6. POST /banque/comptes  (Content-Type: application/json -^> 201 + Location)
echo {"solde": 1500.0, "dateCreation": "2024-11-01", "type": "COURANT"} > "%TEMP%\post.json"
curl.exe -s -i -X POST %BASE% -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@%TEMP%\post.json"
echo.
echo.

echo ### 7. POST /banque/comptes  (Content-Type: application/xml -^> 201 + Location)
echo ^<compte^>^<solde^>1500.0^</solde^>^<dateCreation^>2024-11-01^</dateCreation^>^<type^>COURANT^</type^>^</compte^> > "%TEMP%\post.xml"
curl.exe -s -i -X POST %BASE% -H "Content-Type: application/xml" -H "Accept: application/xml" --data-binary "@%TEMP%\post.xml"
echo.
echo.

echo ### 8. PUT /banque/comptes/4  (Content-Type: application/json -^> 200)
echo {"solde": 2000.0, "dateCreation": "2024-11-01", "type": "EPARGNE"} > "%TEMP%\put.json"
curl.exe -s -i -X PUT %BASE%/4 -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@%TEMP%\put.json"
echo.
echo.

echo ### 9. PUT /banque/comptes/5  (Content-Type: application/xml -^> 200)
echo ^<compte^>^<solde^>2000.0^</solde^>^<dateCreation^>2024-11-01^</dateCreation^>^<type^>EPARGNE^</type^>^</compte^> > "%TEMP%\put.xml"
curl.exe -s -i -X PUT %BASE%/5 -H "Content-Type: application/xml" -H "Accept: application/xml" --data-binary "@%TEMP%\put.xml"
echo.
echo.

echo ### 10. PUT /banque/comptes/9999  (identifiant inconnu -^> 404)
curl.exe -s -i -X PUT %BASE%/9999 -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@%TEMP%\put.json"
echo.
echo.

echo ### 11. DELETE /banque/comptes/4  (-^> 200)
curl.exe -s -i -X DELETE %BASE%/4
echo.
echo.

echo ### 12. DELETE /banque/comptes/4  (deja supprime -^> 404)
curl.exe -s -i -X DELETE %BASE%/4
echo.
echo.

echo ### 13. POST avec un corps JSON invalide (-^> 400 ProblemDetail)
echo {"solde": "abc"} > "%TEMP%\bad.json"
curl.exe -s -i -X POST %BASE% -H "Content-Type: application/json" -H "Accept: application/json" --data-binary "@%TEMP%\bad.json"
echo.
echo.

echo ### 14. GET /banque/comptes/abc  (identifiant non numerique -^> 400)
curl.exe -s -i -H "Accept: application/json" %BASE%/abc
echo.
echo.

echo ### 15. GET /banque/comptes  (Accept: text/html -^> 406 Not Acceptable)
curl.exe -s -i -H "Accept: text/html" %BASE%
echo.
echo.

echo ### 16. Swagger / OpenAPI
echo --- GET /v3/api-docs (extrait du document OpenAPI) ---
curl.exe -s http://localhost:8082/v3/api-docs
echo.
echo --- GET /swagger-ui.html ---
curl.exe -s -o NUL -w "HTTP %%{http_code} -> %%{redirect_url}\n" http://localhost:8082/swagger-ui.html
echo --- GET /swagger-ui/index.html ---
curl.exe -s -o NUL -w "HTTP %%{http_code} (%%{content_type})\n" http://localhost:8082/swagger-ui/index.html
echo.
echo.

echo ### 17. Console H2
curl.exe -s -o NUL -w "GET /h2-console -> HTTP %%{http_code} -> %%{redirect_url}\n" http://localhost:8082/h2-console
echo.
echo =====================================================================
echo  Fin de la demonstration
echo =====================================================================
del "%TEMP%\post.json" "%TEMP%\post.xml" "%TEMP%\put.json" "%TEMP%\put.xml" "%TEMP%\bad.json" 2>nul
endlocal
