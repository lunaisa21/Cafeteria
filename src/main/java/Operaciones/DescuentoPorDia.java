package Operaciones;

import com.datos.DiaSemana;
import com.datos.Promocion;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class DescuentoPorDia implements EstrategiaDescuento {
    private DiaSemana dia;

    @Override
    public double calcularDescuento(double subtotal, Promocion p){
        if (p == null || dia == null){
            return 0.0;
        }
        if (LocalDate.now().getDayOfWeek().equals(dia)) {
            return subtotal * (p.getPorcentaje() / 100.0);
    }
        return 0.0;
    }
}
