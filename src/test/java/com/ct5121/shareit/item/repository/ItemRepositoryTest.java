package com.ct5121.shareit.item.repository;

import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.user.model.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final TestEntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    ItemRepositoryTest(ItemRepository itemRepository,
                       TestEntityManager entityManager,
                       EntityManagerFactory entityManagerFactory) {
        this.itemRepository = itemRepository;
        this.entityManager = entityManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Test
    void shouldSaveItemAndFindItById() {
        User owner = persistUser("Owner", "owner-save@example.com");
        Item item = new Item(null, "Drill", "Powerful drill", true, owner, 100L);

        Item savedItem = itemRepository.saveAndFlush(item);

        entityManager.clear();

        Item foundItem = itemRepository.findById(savedItem.getId()).orElseThrow();

        assertThat(foundItem.getId()).isNotNull();
        assertThat(foundItem.getName()).isEqualTo("Drill");
        assertThat(foundItem.getDescription()).isEqualTo("Powerful drill");
        assertThat(foundItem.isAvailable()).isTrue();
        assertThat(foundItem.getRequestId()).isEqualTo(100L);
    }

    @Test
    void shouldFindItemsByOwnerOrderedByIdAndLoadOwnerAssociation() {
        User owner = persistUser("Owner", "owner-items@example.com");
        Item firstItem = persistItem(owner, "Drill", "Drill description", true, null);
        Item secondItem = persistItem(owner, "Saw", "Saw description", true, null);
        persistItem(persistUser("Other", "other-owner@example.com"), "Bike", "Bike description", true, null);

        entityManager.clear();

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(owner.getId());

        assertThat(items).extracting(Item::getId).containsExactly(firstItem.getId(), secondItem.getId());
        assertThat(items).allSatisfy(item -> {
            assertThat(item.getOwner().getId()).isEqualTo(owner.getId());
            assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(item, "owner")).isTrue();
        });
    }

    @Test
    void shouldSearchOnlyAvailableItemsByTextIgnoringCase() {
        User owner = persistUser("Owner", "owner-search@example.com");
        Item matchingByName = persistItem(owner, "Cordless Drill", "Quiet tool", true, null);
        Item matchingByDescription = persistItem(owner, "Sander", "DRILL for wood", true, null);
        persistItem(owner, "Broken Drill", "Unavailable tool", false, null);
        persistItem(owner, "Lawnmower", "Garden tool", true, null);

        entityManager.clear();

        List<Item> items = itemRepository.searchItems(
                "drill",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(items)
                .extracting(Item::getId)
                .containsExactly(matchingByName.getId(), matchingByDescription.getId());
    }

    @Test
    void shouldFindItemsByRequestId() {
        User owner = persistUser("Owner", "owner-request@example.com");
        Item requestedItem = persistItem(owner, "Tent", "Camping tent", true, 77L);
        persistItem(owner, "Stove", "Camping stove", true, 88L);

        entityManager.clear();

        List<Item> items = itemRepository.findByRequestId(77L);

        assertThat(items).singleElement().extracting(Item::getId).isEqualTo(requestedItem.getId());
    }

    private User persistUser(String name, String email) {
        return entityManager.persistAndFlush(new User(null, name, email, "password", LocalDateTime.now()));
    }

    private Item persistItem(User owner, String name, String description, boolean available, Long requestId) {
        return entityManager.persistAndFlush(new Item(null, name, description, available, owner, requestId));
    }
}
