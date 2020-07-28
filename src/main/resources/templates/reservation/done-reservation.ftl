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
                            <div class="row">
                                <div class="col-12">
                                    <h1>Hola, ${restaurant_name}!</h1>
                                    <p class="lead">Esta es una reserva válida de ${name} ${lastname}</p>
                                    <hr class="my-4">

                                    <p>
                                        La reserva está compuesta de la siguiente forma:
                                    </p>

                                    <ul>
                                        <#list membershipMap as key,value>
                                            <#if value gt 0 >
                                                <li>Membresia ${key} : ${value} (persona/s) </li>
                                            </#if>
                                        </#list>
                                    </ul>

                                    <p>
                                        Los platos disponibles para esta reserva son:
                                    </p>

                                    <#list dishesMap as key,value>
                                        ${key}
                                        <ul>
                                            <#list value>
                                                <#items as dish>
                                                    <li>  ${dish.name} : ${dish.description}</li>
                                                </#items>
                                            </#list>
                                        </ul>
                                    </#list>
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