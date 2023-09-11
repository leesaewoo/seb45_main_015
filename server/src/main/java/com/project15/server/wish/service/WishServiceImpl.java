package com.project15.server.wish.service;

import com.project15.server.exception.ExceptionCode;
import com.project15.server.exception.GlobalException;
import com.project15.server.item.entity.Item;
import com.project15.server.item.repository.ItemRepository;
import com.project15.server.item.service.ItemServiceImpl;
import com.project15.server.member.entity.Member;
import com.project15.server.member.repository.MemberRepository;
import com.project15.server.member.service.MemberService;
import com.project15.server.wish.entity.Wish;
import com.project15.server.wish.mapper.WishMapper;
import com.project15.server.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
@RequiredArgsConstructor
public class WishServiceImpl implements WishService{

    private final WishRepository wishRepository;

    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    private final ItemServiceImpl itemService;

    private final MemberService memberService;

    private final WishMapper wishMapper;

    @Override
    public void createWish(Wish wish) {
        Item findItem = itemService.findVerifiedItem(wish.getItem().getItemId());

        Member fineMember = memberRepository.findById(wish.getMember().getMemberId())
                .orElseThrow(() -> new GlobalException(ExceptionCode.MEMBER_NOT_FOUND));

        if(wishRepository.findByMemberMemberIdAndItemItemId(wish.getMember().getMemberId(), wish.getItem().getItemId()).isPresent()) {
            throw new GlobalException(ExceptionCode.WISH_EXIST);
        }

        wishRepository.save(wish);
    }

    @Override
    public void removeWishes(Long memberId, List<Long> itemIds) {
        //TODO: List로 들어온 item id와 member id로 wish 찾아서 삭제

        itemIds.forEach(itemId -> removeWish(memberId, itemId));
    }

    private void removeWish(Long memberId, Long itemId) {
        Wish findWish = wishRepository.findByMemberMemberIdAndItemItemId(memberId, itemId)
                .orElseThrow(() -> new GlobalException(ExceptionCode.WISH_NOT_FOUND));

        wishRepository.delete(findWish);
    }

    @Override
    public Page<Wish> findWishes(int pageNumber, int pageSize, Long memberId) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createdAt").descending());

        Page<Wish> wishPage = wishRepository.findByMemberMemberId(memberId, pageable);

        return wishPage;
    }
}
