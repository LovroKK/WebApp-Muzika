package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.IzvodacKorisnik;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IzvodacProfilResponse {

    private String usernameIzvodac;
    private String logoAvatar;
    private String ime;
    private String prezime;
    private String email;
    private String brojTelefona;
    private String linkMixtape;
    private BigDecimal cijenaPoSatu;
    private String kratkiOpis;
    private String prijasnjiPoslovi;
    private Integer radiOd;
    private Integer ukupnoGodinaIskustva;

    public static IzvodacProfilResponse from(IzvodacKorisnik i) {
        IzvodacProfilResponse r = new IzvodacProfilResponse();
        r.setUsernameIzvodac(i.getUsernameIzvodac());
        r.setLogoAvatar(i.getLogoAvatar());
        r.setIme(i.getIme());
        r.setPrezime(i.getPrezime());
        r.setEmail(i.getEmail());
        r.setBrojTelefona(i.getBrojTelefona());
        r.setLinkMixtape(i.getLinkMixtape());
        r.setCijenaPoSatu(i.getCijenaPoSatu());
        r.setKratkiOpis(i.getKratkiOpis());
        r.setPrijasnjiPoslovi(i.getPrijasnjiPoslovi());
        r.setRadiOd(i.getRadiOd());
        r.setUkupnoGodinaIskustva(i.getUkupnoGodinaIskustva());
        return r;
    }
}
