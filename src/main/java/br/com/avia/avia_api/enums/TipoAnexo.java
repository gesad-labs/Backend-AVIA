package br.com.avia.avia_api.enums;

public enum TipoAnexo {
    PDF("application/pdf", "pdf"),
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpg"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final String mime;
    private final String extensao;

    TipoAnexo(String mime, String extensao){
        this.mime = mime;
        this.extensao = extensao;
    }
    public String mime(){ return mime;}
    public String extensao(){ return extensao;}
}
