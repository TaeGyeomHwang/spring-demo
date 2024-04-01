package com.shop.repository.Item;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import lombok.RequiredArgsConstructor;

import java.util.List;
@RequiredArgsConstructor
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QItem qItem = QItem.item;

    @Override
    public List<Item> queryDslTest() {

        return queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%"+"테스트 상품 상세 설명"+"%"))
                .orderBy(qItem.price.desc())
                .fetch();
    }
}
