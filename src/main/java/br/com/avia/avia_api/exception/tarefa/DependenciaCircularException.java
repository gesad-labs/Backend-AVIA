package br.com.avia.avia_api.exception.tarefa;

public class DependenciaCircularException extends RuntimeException {

    public DependenciaCircularException() {
        super("Essa dependência criaria um ciclo de bloqueios em que nenhuma tarefa poderia ser concluída");
    }
}