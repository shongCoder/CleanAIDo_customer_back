package org.zerock.cleanaido_customer_back.product.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.zerock.cleanaido_customer_back.category.entity.QCategory;
import org.zerock.cleanaido_customer_back.common.dto.PageRequestDTO;
import org.zerock.cleanaido_customer_back.common.dto.PageResponseDTO;
import org.zerock.cleanaido_customer_back.customer.entity.QCustomer;
import org.zerock.cleanaido_customer_back.order.entity.QOrder;
import org.zerock.cleanaido_customer_back.order.entity.QOrderDetail;
import org.zerock.cleanaido_customer_back.product.dto.ProductListDTO;
import org.zerock.cleanaido_customer_back.product.entity.*;

import java.util.List;

@Log4j2
public class ProductSearchImpl extends QuerydslRepositorySupport implements ProductSearch {

    public ProductSearchImpl() {
        super(Product.class);
    }

    @Override
    public PageResponseDTO<ProductListDTO> list(PageRequestDTO pageRequestDTO) {

        QProduct product = QProduct.product;
        QImageFile imageFile = QImageFile.imageFile;
        QReview review = QReview.review;
        QCategory category = QCategory.category;

        JPQLQuery<Product> query = from(product).where(product.pstatus.eq("selling"));

        query.leftJoin(product.imageFiles, imageFile).on(imageFile.ord.eq(0));
        query.leftJoin(product.category, category).on(category.cno.eq(product.category.cno));
        query.leftJoin(review).on(review.product.eq(product));
        query.groupBy(product);
        query.orderBy(product.pno.desc());


        Pageable pageable =
                PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize());

        getQuerydsl().applyPagination(pageable, query);

        JPQLQuery<ProductListDTO> results =
                query.select(
                        Projections.bean(
                                ProductListDTO.class,
                                product.pno,
                                product.pname,
                                product.price,
                                product.pstatus,
                                imageFile.fileName,
                                review.count().as("reviewCount"),
                                review.score.avg().round().castToNum(Integer.class).as("score"),
                                product.category.cname.as("category")
                        )

                );


        List<ProductListDTO> dtoList = results.fetch();

        log.info("--------------------");
        log.info(dtoList);

        long total = query.fetchCount();

        return PageResponseDTO.<ProductListDTO>withAll().
                dtoList(dtoList).
                totalCount(total).
                pageRequestDTO(pageRequestDTO).
                build();
    }

    @Override
    public PageResponseDTO<ProductListDTO> searchBy(String type, String keyword, PageRequestDTO pageRequestDTO) {
        QProduct product = QProduct.product;
        QImageFile imageFile = QImageFile.imageFile;
        QCategory category = QCategory.category;
        QReview review = QReview.review;


        JPQLQuery<Product> query = from(product);
        query.leftJoin(product.imageFiles, imageFile).on(imageFile.ord.eq(0));
        query.leftJoin(product.category, category).on(category.cno.eq(product.category.cno));
        query.leftJoin(review).on(review.product.eq(product));
        query.groupBy(product);
        query.orderBy(product.pno.desc());

        log.info("Type = " + type);

        if (type == null || type.isEmpty()) {
            BooleanBuilder builder = new BooleanBuilder();
            builder.or(category.cname.like("%" + keyword + "%"))
                    .or(product.pname.like("%" + keyword + "%"))
                    .or(product.ptags.like("%" + keyword + "%"));
            query.where(builder).distinct();
            log.info("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe");
        } else if (type.equals("category")) {
            query.where(category.cname.like("%" + keyword + "%"));
            log.info("ddddddddddddddddddddddddddddddddddddddddd");

        }

        query.orderBy(product.pno.desc());


        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize());


        getQuerydsl().applyPagination(pageable, query);

        JPQLQuery<ProductListDTO> results =
                query.select(
                        Projections.bean(
                                ProductListDTO.class,
                                product.pno,
                                product.pname,
                                product.price,
                                product.pstatus,
                                imageFile.fileName,
                                review.count().as("reviewCount"),
                                review.score.avg().round().castToNum(Integer.class).as("score"),
                                product.category.cname.as("category")
                        )

                );

        List<ProductListDTO> dtoList = results.fetch();
        log.info(results);

        long total = query.fetchCount();

        return PageResponseDTO.<ProductListDTO>withAll()
                .dtoList(dtoList)
                .totalCount(total)
                .pageRequestDTO(pageRequestDTO)
                .build();
    }
    
    // 랜덤으로 가져오는 추천상품 리스트
    public List<ProductListDTO> listSuggest() {

        QProduct product = QProduct.product;
        QImageFile imageFile = QImageFile.imageFile;
        QReview review = QReview.review;
        QCategory category = QCategory.category;

        JPQLQuery<Product> query = from(product).where(product.pstatus.eq("selling"));

        query.leftJoin(product.imageFiles, imageFile).on(imageFile.ord.eq(0));
        query.leftJoin(product.category, category).on(category.cno.eq(product.category.cno));
        query.leftJoin(review).on(review.product.eq(product));
        query.groupBy(product);
        query.limit(10);
        query.orderBy(Expressions.numberTemplate(Double.class, "function('RAND')").asc());

        JPQLQuery<ProductListDTO> results =
                query.select(
                        Projections.bean(
                                ProductListDTO.class,
                                product.pno,
                                product.pname,
                                product.price,
                                product.pstatus,
                                imageFile.fileName,
                                review.count().as("reviewCount"),
                                review.score.avg().round().castToNum(Integer.class).as("score"),
                                product.category.cname.as("category")
                        )
                );

        List<ProductListDTO> dtoList = results.fetch();

        log.info("Randomly fetched products: {}", dtoList);

        return dtoList; // PageResponseDTO 없이 리스트 반환
    }

    // 자주 구매한 상품
    @Override
    public PageResponseDTO<ProductListDTO> listFreq(String customerId, PageRequestDTO pageRequestDTO) {
        QProduct product = QProduct.product;
        QImageFile imageFile = QImageFile.imageFile;
        QOrderDetail orderDetail = QOrderDetail.orderDetail;
        QOrder order = QOrder.order;
        QCustomer customer = QCustomer.customer;

        // Query 구성
        JPQLQuery<Product> query = from(product)
                .leftJoin(product.imageFiles, imageFile).on(imageFile.ord.eq(0))
                .leftJoin(orderDetail).on(orderDetail.product.eq(product))
                .leftJoin(order).on(orderDetail.order.eq(order))
                .leftJoin(customer).on(order.customer.eq(customer))
                .where(customer.customerId.eq(customerId))
                .groupBy(product.pno)
                .orderBy(orderDetail.count().desc());

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize());
        getQuerydsl().applyPagination(pageable, query);

        // DTO 변환
        JPQLQuery<ProductListDTO> results = query.select(
                Projections.bean(
                        ProductListDTO.class,
                        product.pno,
                        product.pname,
                        product.price,
                        imageFile.fileName.as("fileName"),
                        orderDetail.count().as("purchaseCount")
                )
        );

        List<ProductListDTO> dtoList = results.fetch();

        long total = query.fetchCount();

        return PageResponseDTO.<ProductListDTO>withAll()
                .dtoList(dtoList)
                .totalCount(total)
                .pageRequestDTO(pageRequestDTO)
                .build();
    }


}
