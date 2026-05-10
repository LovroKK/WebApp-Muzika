package hr.beatsync.backend.dto;

import hr.beatsync.backend.model.BusinessKorisnik;
import lombok.Data;

@Data
public class BusinessProfilResponse {

    private String usernameBusiness;
    private String logoAvatar;
    private String nazivKluba;
    private String lokacija;
    private String opis;
    private String residentDjUsername;
    private String najboljiIzvodaci;
    private String email;
    private String brojTelefona;

    public static BusinessProfilResponse from(BusinessKorisnik b) {
        BusinessProfilResponse r = new BusinessProfilResponse();
        r.setUsernameBusiness(b.getUsernameBusiness());
        r.setLogoAvatar(b.getLogoAvatar());
        r.setNazivKluba(b.getNazivKluba());
        r.setLokacija(b.getLokacija());
        r.setOpis(b.getOpis());
        r.setNajboljiIzvodaci(b.getNajboljiIzvodaci());
        r.setEmail(b.getEmail());
        r.setBrojTelefona(b.getBrojTelefona());
        if (b.getResidentDj() != null) {
            r.setResidentDjUsername(b.getResidentDj().getUsernameIzvodac());
        }
        return r;
    }
}
