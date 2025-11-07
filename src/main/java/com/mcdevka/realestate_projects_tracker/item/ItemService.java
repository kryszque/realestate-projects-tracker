package com.mcdevka.realestate_projects_tracker.item;

import com.mcdevka.realestate_projects_tracker.item.document.Document;
import com.mcdevka.realestate_projects_tracker.item.meeting.Meeting;
import com.mcdevka.realestate_projects_tracker.item.task.Task;
import com.mcdevka.realestate_projects_tracker.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.pillar.PillarRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final PillarRepository pillarRepository;

    public ItemService(ItemRepository itemRepository, PillarRepository pillarRepository) {
        this.itemRepository = itemRepository;
        this.pillarRepository = pillarRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsForPillar(Long pillarId) {
        if (!pillarRepository.existsById(pillarId)) {
            throw new IllegalArgumentException("Filar o ID " + pillarId + " nie istnieje.");
        }

        return itemRepository.findByPillarId(pillarId);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found!"));
    }

    @Transactional
    public Item createItem(Long pillarId, Item item) {
        Pillar pillar = pillarRepository.findById(pillarId)
                .orElseThrow(() -> new IllegalArgumentException("Filar o ID " + pillarId + " nie istnieje."));

        item.setAddDate(LocalDate.now());
        item.setPillar(pillar);
        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long itemId, Item updatedItemData) {
        Item existingItem = getItemById(itemId);

        if (!existingItem.getClass().equals(updatedItemData.getClass())) {
            throw new IllegalArgumentException("Nie można zmienić typu Itema (np. z TextItem na MeetingItem).");
        }

        if (existingItem instanceof Task) {

        } else if (existingItem instanceof Meeting) {

        } else if (existingItem instanceof Document) {
            Document existingDoc = (Document) existingItem;
            Document updatedDoc = (Document) updatedItemData;

            existingDoc.setDeadline(updatedDoc.getDeadline());
            existingDoc.setDescription(updatedDoc.getDescription());
            existingDoc.setLastChangeDate(LocalDate.now());
            existingDoc.setName(updatedDoc.getName());
            existingDoc.setStatus(updatedDoc.getStatus());
        }

        return itemRepository.save(existingItem);
    }

    public Item archiveItem(Long id){
        Item archvedItem = getItemById(id);
        archvedItem.setState("archived");
        return itemRepository.save(archvedItem);
    }

    public Item finishItem(Long id){
        Item finishedItem = getItemById(id);
        finishedItem.setState("finished");
        return itemRepository.save(finishedItem);
    }
}
