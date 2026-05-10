package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.IzvodacKorisnik;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DjSearchResponse {

    private String usernameIzvodac;
    private String logoAvatar;
    private String ime;
    private String prezime;
    private String kratkiOpis;
    private BigDecimal cijenaPoSatu;
    private Integer ukupnoGodinaIskustva;
    private String linkMixtape;

    public static DjSearchResponse from(IzvodacKorisnik i) {
        DjSearchResponse r = new DjSearchResponse();
        r.setUsernameIzvodac(i.getUsernameIzvodac());
        r.setLogoAvatar(i.getLogoAvatar());
        r.setIme(i.getIme());
        r.setPrezime(i.getPrezime());
        r.setKratkiOpis(i.getKratkiOpis());
        r.setCijenaPoSatu(i.getCijenaPoSatu());
        r.setUkupnoGodinaIskustva(i.getUkupnoGodinaIskustva());
        r.setLinkMixtape(i.getLinkMixtape());
        return r;
    }
}
