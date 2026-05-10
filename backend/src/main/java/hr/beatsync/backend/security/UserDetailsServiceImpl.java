package hr.beatsync.backend.security;

import hr.beatsync.backend.model.BusinessKorisnik;
import hr.beatsync.backend.model.IzvodacKorisnik;
import hr.beatsync.backend.repository.BusinessKorisnikRepository;
import hr.beatsync.backend.repository.IzvodacKorisnikRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IzvodacKorisnikRepository izvodacRepo;
    private final BusinessKorisnikRepository businessRepo;

    public UserDetailsServiceImpl(IzvodacKorisnikRepository izvodacRepo,
                                  BusinessKorisnikRepository businessRepo) {
        this.izvodacRepo = izvodacRepo;
        this.businessRepo = businessRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Prvo probaj izvođača
        Optional<IzvodacKorisnik> izvodacOpt = izvodacRepo.findById(username);
        if (izvodacOpt.isPresent()) {
            IzvodacKorisnik i = izvodacOpt.get();
            return new User(
                    i.getUsernameIzvodac(),
                    i.getLozinka(),
                    List.of(new SimpleGrantedAuthority("ROLE_IZVODAC"))
            );
        }

        // Zatim probaj business korisnika
        Optional<BusinessKorisnik> businessOpt = businessRepo.findById(username);
        if (businessOpt.isPresent()) {
            BusinessKorisnik b = businessOpt.get();
            return new User(
                    b.getUsernameBusiness(),
                    b.getLozinka(),
                    List.of(new SimpleGrantedAuthority("ROLE_BUSINESS"))
            );
        }

        throw new UsernameNotFoundException("Korisnik nije pronađen: " + username);
    }
}