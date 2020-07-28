<!doctype html>
<html style=" background-color: #e9ecef" lang="en">
<head>
    <#include "../common/head.ftl"/>
</head>
<body>
<div style=" background-color: #e9ecef" class="container-fluid">
    <#include "../common/header.ftl"/>
    <div class="row">
        <div class="col-sm">
            <div class="row">
                <div class="col-12">
                    <div class="jumbotron jumbotron-fluid">
                        <div class="container">
                            <div class="row align-items-center">
                                <img style="width:100px" class="mr-2" src="https://i.ibb.co/kMvrQqH/png-transparent-check-logo-check-mark-computer-icons-correct-miscellaneous-angle-text-thumbnail-1.png" alt="confirm-1174801-1280" border="0">
                                <h1>Email confirmado</h1>
                            </div>
                            <div class="row">
                                <div class="col-12">
                                    <p class="lead mt-3">Genial, ${name}! Ya confirmaste tu email</p>
                                    <hr class="my-4">
                                    <p>A partir de ahora, recibirás en tu correo <b>${email}</b> todo lo que necesites!</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "../common/footer.ftl"/>

<#include "../common/scripts.ftl"/>
</body>
</html>
