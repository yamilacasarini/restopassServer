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
                                <img style="width:100px" class="mr-2" src="https://i.ibb.co/5Gg7m2r/cancel-1174809-1280.png"
                                     alt="cancel-1174809-1280" border="0">
                                <h1>Reserva cancelada</h1>
                            </div>
                            <div class="row">
                                <div class="col-12">
                                    <p class="lead mt-3">La reserva ${reservationId} fue cancelada por <span class="font-weight-bold">${bookingOwner}</span>.</p>
                                    <hr class="my-4">
                                    <p>
                                       Puedes gestionar todas tus reservas a través de la app.
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