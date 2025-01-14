package org.zerock.cleanaido_customer_back.cart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.cleanaido_customer_back.cart.dto.CartDetailDTO;
import org.zerock.cleanaido_customer_back.cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Log4j2
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping("list")
    public ResponseEntity<List<CartDetailDTO>> list(
            @RequestParam(value = "customerId", required = false) String customerId
    ) {
        log.info("-----kkkkkkkkkkkkkk-------");
        log.info(customerId);
        return ResponseEntity.ok(cartService.listCartDetail(customerId));
    }

    @DeleteMapping("")
    public Long delete(
            @RequestParam(value = "cdno", required = false) Long cdno
    ){
        return cartService.deleteCartDetail(cdno);
    }

    @PutMapping("")
    public Long update(
            @RequestParam(value = "cdno", required = false) Long cdno,
            @RequestParam(value = "quantity", required = false) int quantity
    ){
        log.info("------upadate cart-----------");
        return cartService.updateQty(cdno, quantity);
    }
}
