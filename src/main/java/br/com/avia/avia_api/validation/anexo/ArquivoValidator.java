package br.com.avia.avia_api.validation.anexo;


import br.com.avia.avia_api.enums.TipoAnexo;
import br.com.avia.avia_api.exception.anexo.ArquivoMuitoGrandeException;
import br.com.avia.avia_api.exception.anexo.ArquivoVazioException;
import br.com.avia.avia_api.exception.anexo.TipoNaoPermitidoException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@Component
public class ArquivoValidator {

    public static final Long TAMANHO_MAXIMO = 25L * 1024 * 1024;
    public static final int MAX_ENTRADAS_ZIP = 1000;
    // verificação da assinatura dos arquivos, impedindo o usuario de disfaçar um arquivo como outro
    private static final byte[] PDF  = {0x25, 0x50, 0x44, 0x46, 0x2D};
    private static final byte[] PNG  = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] ZIP  = {0x50, 0x4B, 0x03, 0x04};

    public TipoAnexo validar(MultipartFile arquivo){
        if(arquivo == null || arquivo.isEmpty())
            throw new ArquivoVazioException();
        if (arquivo.getSize() > TAMANHO_MAXIMO)
            throw new ArquivoMuitoGrandeException();

        byte[] cabecalho = lerCabecalho(arquivo);

        if(comecaCom(cabecalho, PDF)) return TipoAnexo.PDF;
        if(comecaCom(cabecalho, PNG)) return TipoAnexo.PNG;
        if(comecaCom(cabecalho, JPEG)) return TipoAnexo.JPEG;
        if(comecaCom(cabecalho, ZIP) && ehDocx(arquivo)) return TipoAnexo.DOCX;

        throw new TipoNaoPermitidoException();
    }

    private byte[] lerCabecalho(MultipartFile arquivo){
        try(InputStream in = arquivo.getInputStream()){
            return in.readNBytes(16);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean comecaCom(byte[] cabecalho, byte[] assinatura){
        return cabecalho.length >= assinatura.length // garantir q o cabecalho é maior ou igual a assinatura para n dar erro
                // como funciona, tu começa no cabecalho q vai de 0 ate assinatura.length, e compara com assinatura q vai de 0 ate assinatura.length
               && Arrays.equals(cabecalho, 0, assinatura.length, assinatura, 0, assinatura.length);
    }

    private boolean ehDocx(MultipartFile arquivo){
        Path temp = null;
        try {
            // cria um arquivo temporario ler o bytes do upload e para verificar os o indice do ZIP
            temp = Files.createTempFile("anexo-", ".tmp");
            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            try (ZipFile zip = new ZipFile(temp.toFile())){
                return zip.size() <= MAX_ENTRADAS_ZIP
                        // entradas que definem um Docx
                        && zip.getEntry("[Content_Types].xml") != null
                        && zip.getEntry("word/document.xml") != null
                        // rejeitar arquivos com macros
                        // pode ser removivel, tu que decide davi
                        && zip.getEntry("word/vbaProject.bin") == null;
            }
            // zip corrompido etc
        } catch (ZipException ex){
            return false;
        } catch (IOException ex){
            throw new UncheckedIOException(ex);

            // roda sempre, dando exception ou nao, limpa o arquivo temporario que se torna lixo dps do programa
        } finally {
            if(temp != null){
                try {
                   Files.deleteIfExists(temp);
                } catch (IOException ignored) { }
            }
        }
    }
}
