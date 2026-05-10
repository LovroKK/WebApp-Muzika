package hr.beatsync.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoritResponse {
    private String tipFavorita;
    private String favoritUsername;
}
