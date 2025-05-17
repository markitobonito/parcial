
$(document).ready(function () {
    $('#tablaAsistencias').DataTable({
        processing: true,
        serverSide: true,
        ajax: {
            url: '/coordinador/asistencias-datatable',
            type: 'GET'
        },
        columns: [
            {
                data: 'fecha',
                render: function (data) {
                    return moment(data).format('DD/MM/YYYY');
                }
            },
            { data: 'horaInicio' },
            { data: 'horaFin' },
            { data: 'lugarExacto' },
            {
                data: 'estado',
                render: function (data) {
                    let badgeClass = 'bg-secondary';
                    if (data === 'Asistió') badgeClass = 'bg-success';
                    else if (data === 'En Curso') badgeClass = 'bg-warning';
                    return `<span class="badge ${badgeClass}">${data}</span>`;
                }
            }
        ],
        order: [[0, 'desc']],
        language: {
            url: '/lang/es-ES.json'
        }
    });
});
