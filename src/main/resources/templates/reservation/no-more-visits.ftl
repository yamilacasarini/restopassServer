<!doctype html>
<html style=" background-color: #e9ecef" lang="en">
<head>
    <#include "common/head.ftl"/>
</head>
<body>
<div style=" background-color: #e9ecef" class="container-fluid">
    <#include "common/header.ftl"/>
    <div class="row">
        <div class="col-sm">
            <div class="row">
                <div class="col-12">
                    <div class="jumbotron jumbotron-fluid">
                        <div class="container">
                            <div class="row align-items-center">
                                <img style="width:100px" class="mr-2" src="https://i.ibb.co/9gLnkbq/1200px-Flat-cross-icon-svg.png"
                                     alt="cancel-1174809-1280" border="10">
                                <h1>     Ups! Parece que ya no te quedan mas visitas</h1>
                            </div>
                            <div class="row">
                                <div class="col-12">
                                    <p class="lead mt-3">${msg}. No te preocupes! Guardaremos tu invitación</span>.</p>
                                    <hr class="my-4">
                                    <p>
                                       Puedes cancelar alguna reserva que no hayas asistido aún o conseguir un plan con mas visitas a través de la app.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "common/footer.ftl"/>

<#include "common/scripts.ftl"/>
</body>
</html>