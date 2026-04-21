@echo off
REM Fix "Packet for query is too large" by raising MySQL max_allowed_packet to 32 MB.
REM Run this once (from Command Prompt or double-click). You will be asked for the MySQL root password.

echo Setting MySQL max_allowed_packet to 32 MB...
echo.

REM Try mysql from PATH first; if not found, try common Windows install path
where mysql >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mysql -u root -p -e "SET GLOBAL max_allowed_packet = 33554432;"
) else (
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SET GLOBAL max_allowed_packet = 33554432;"
    ) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "SET GLOBAL max_allowed_packet = 33554432;"
    ) else (
        echo MySQL not found in PATH or in Program Files.
        echo Run this in Command Prompt instead:
        echo   mysql -u root -p -e "SET GLOBAL max_allowed_packet = 33554432;"
        echo Or add MySQL bin folder to your PATH.
        pause
        exit /b 1
    )
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo Done. Restart your Spring Boot app and try the event update again.
) else (
    echo.
    echo Failed. Check your MySQL user (root) and password.
)
echo.
pause
