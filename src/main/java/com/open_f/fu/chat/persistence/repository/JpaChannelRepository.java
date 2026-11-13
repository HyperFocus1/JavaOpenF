package com.open_f.fu.chat.persistence.repository;

import java.util.Optional;

import com.open_f.fu.chat.domain.exception.ChannelNotFoundException;
import com.open_f.fu.chat.domain.interfaces.ChannelRepository;
import com.open_f.fu.chat.domain.model.Channel;
import com.open_f.fu.chat.persistence.entity.ChannelEntity;
import com.open_f.fu.chat.persistence.mapper.ChannelMapper;
import com.open_f.fu.shared.persistence.JpaRepository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaChannelRepository extends JpaRepository<ChannelEntity> implements ChannelRepository {

    private final EntityManager em;

    @Inject
    public JpaChannelRepository(EntityManager em) {
        super(ChannelEntity.class);
        this.em = em;
    }

    @Override
    public Channel save(Channel channel) {
        if (channel.getId() == null) {
            return ChannelMapper.toDomain(persist(this.em, ChannelMapper.toNewEntity(channel)));
        }
        ChannelEntity e = findEntity(this.em, channel.getId())
                .orElseThrow(() -> new ChannelNotFoundException(channel.getId()));
        ChannelMapper.copy(channel, e);
        return ChannelMapper.toDomain(e);
    }

    @Override
    public Optional<Channel> findById(Long id) {
        return findEntity(this.em, id).map(ChannelMapper::toDomain);
    }

    @Override
    public Optional<Channel> findFirstByTitleContaining(String text) {
        return this.em.createQuery("""
                SELECT c FROM ChannelEntity c
                WHERE c.title LIKE :title
                ORDER BY c.id
                """, ChannelEntity.class)
                .setParameter("title", "%" + text + "%")
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(ChannelMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return findEntity(this.em, id).isPresent();
    }
}
