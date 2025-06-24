@echo off
echo === Running Tests for Device Management System ===

echo Running unit tests...
call mvn test

if %ERRORLEVEL% equ 0 (
    echo.
    echo All tests passed successfully!
    echo.
    echo Test coverage includes:
    echo - Device service tests
    echo - Controller tests  
    echo - Integration tests
    echo - Repository tests
    echo.
) else (
    echo.
    echo Some tests failed. Please check the output above.
    exit /b 1
)

pause
