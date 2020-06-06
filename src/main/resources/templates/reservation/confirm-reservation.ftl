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
                            <div class="row">
                                <div class="col-12">
                                    <h1>Genial, ${name}!</h1>
                                    <p class="lead">Confirmaste la reserva</p>
                                    <hr class="my-4">
                                    <p>
                                        Te enviamos un email con el código QR a
                                        <span class="font-weight-bold"> ${email}</span>. Podrás gestionar la reserva a través de la app.
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