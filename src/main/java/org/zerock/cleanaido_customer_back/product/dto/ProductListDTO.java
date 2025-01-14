package org.zerock.cleanaido_customer_back.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zerock.cleanaido_customer_back.category.entity.Category;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductListDTO {
    private Long pno;
    private String pname;
    private int price;
    private String pstatus;
    private String fileName;
    private Long reviewCount;
    private int score;
    private String category;
}
