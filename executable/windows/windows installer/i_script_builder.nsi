# define installer name
OutFile "install.exe"
 
!define COMPANYNAME "WIT_ROV"
# set desktop as install directory
InstallDir $APPDATA

# default section start
Section
 
# define output path
SetOutPath $INSTDIR\${COMPANYNAME}\lib
File lib\hidapijni64.dll
File lib\hidapijni32.dll

# define output path
SetOutPath $INSTDIR\${COMPANYNAME}\DB
File DB\wit_rov.db
 
SetOutPath $INSTDIR\${COMPANYNAME}
File wit_rov.exe
   
# define uninstaller name
WriteUninstaller $INSTDIR\${COMPANYNAME}\UNINSTALL.exe

CreateShortCut "$DESKTOP\${COMPANYNAME}.lnk" "$INSTDIR\${COMPANYNAME}\wit_rov.exe" ""
 
CreateDirectory "$SMPROGRAMS\${COMPANYNAME}"
CreateShortCut "$SMPROGRAMS\${COMPANYNAME}\Uninstall.lnk" "$INSTDIR\${COMPANYNAME}\UNINSTALL.exe" "" "$INSTDIR\${COMPANYNAME}\UNINSTALL.exe" 0
CreateShortCut "$SMPROGRAMS\${COMPANYNAME}\WIT_ROV.lnk" "$INSTDIR\${COMPANYNAME}\wit_rov.exe" "" "$INSTDIR\${COMPANYNAME}\wit_rov.exe" 0
 
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}" \
                 "DisplayName" "${COMPANYNAME}"
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}" \
                 "UninstallString" "$\"$INSTDIR\${COMPANYNAME}\UNINSTALL.exe$\""
 
#-------
# default section end
SectionEnd
 
# create a section to define what the uninstaller does.
# the section will always be named "Uninstall"
Section "Uninstall"
 
# Always delete uninstaller first
Delete $INSTDIR\UNINSTALL.exe
 
# now delete installed file
Delete $INSTDIR\lib\hidapijni32.dll
Delete $INSTDIR\lib\hidapijni64.dll
RMDir "$INSTDIR\lib"
Delete $INSTDIR\DB\wit_rov.db
RMDir "$INSTDIR\DB"
Delete $INSTDIR\wit_rov.exe
SetOutPath $APPDATA
RMDir /r /REBOOTOK "$INSTDIR\${COMPANYNAME}"

Delete "$SMPROGRAMS\${COMPANYNAME}\Uninstall.lnk"
Delete "$SMPROGRAMS\${COMPANYNAME}\WIT_ROV.lnk"
RMDir /r /REBOOTOK "$SMPROGRAMS\${COMPANYNAME}"
Delete $DESKTOP\${COMPANYNAME}.lnk

DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME}"

SectionEnd