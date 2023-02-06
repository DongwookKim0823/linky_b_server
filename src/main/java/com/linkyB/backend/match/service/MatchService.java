package com.linkyB.backend.match.service;

import com.linkyB.backend.block.converter.BlockConverter;
import com.linkyB.backend.block.dto.BlockDto;
import com.linkyB.backend.block.entity.Block;
import com.linkyB.backend.block.mapper.BlockMapper;
import com.linkyB.backend.block.repository.BlockRepository;
import com.linkyB.backend.match.converter.MatchConverter;
import com.linkyB.backend.match.dto.MatchDto;
import com.linkyB.backend.match.entity.Match;
import com.linkyB.backend.match.entity.MatchStatus;
import com.linkyB.backend.match.entity.status;
import com.linkyB.backend.match.mapper.MatchMapper;
import com.linkyB.backend.match.repository.MatchRepository;
import com.linkyB.backend.user.domain.User;
import com.linkyB.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final BlockRepository blockRepository;
    private final MatchConverter matchConverter;
    private final BlockConverter blockConverter;

    // 매칭 시도
    @Transactional
    public MatchDto matching(long userId, long userGetMatched) {

        User Matching = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
        User GetMatched = userRepository.findById(userGetMatched)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        Match entity = matchRepository.save(matchConverter.tryMatching(Matching, GetMatched));
        MatchDto dto = MatchMapper.INSTANCE.entityToDto(entity);

        return dto;
    }

    // 매칭 수락
    @Transactional
    public MatchDto accept(long matchId, long userId) {

        Match entity = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 연결내역이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        // userId와 매칭을 받은 유저의 인덱스가 같다면 수락 처리
        if (entity.getUserMatching().getUserId() == user.getUserId()) {
            entity.update(MatchStatus.ACTIVE);
            MatchDto dto = MatchMapper.INSTANCE.entityToDto(entity);

            return dto;

            // 다르다면 예외처리
        } else
            throw new RuntimeException("수락 권한이 없습니다.");

    }

    // 매칭 거절
    @Transactional
    public BlockDto refuse(long matchId, long userId) {

        Match entity = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 연결내역이 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

        // userId와 매칭을 받은 유저의 인덱스가 같다면 수락 처리
        if (entity.getUserMatching().getUserId() == user.getUserId()) {
            entity.updateMatch(status.INACTIVE);

            Block block = blockRepository.save(blockConverter.block(entity.getUserGetMatched(), entity.getUserMatching()));
            BlockDto dto = BlockMapper.INSTANCE.entityToDto(block);
            return dto;
        } else
            throw new RuntimeException("거절 권한 없습니다.");
    }
}
