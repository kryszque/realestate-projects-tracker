package com.mcdevka.realestate_projects_tracker.item;

import com.mcdevka.realestate_projects_tracker.item.document.Document;
import com.mcdevka.realestate_projects_tracker.item.document.DocumentHistory;
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

    private Pillar validatePillarPath(Long projectId, Long pillarId) {
        Pillar pillar = pillarRepository.findById(pillarId)
                .orElseThrow(() -> new IllegalArgumentException("Filar o ID " + pillarId + " nie istnieje."));

        if (!pillar.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Filar o ID " + pillarId + " nie należy do projektu o ID " + projectId);
        }
        return pillar;
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsForPillar(Long projectId, Long pillarId) {
        validatePillarPath(projectId, pillarId);

        return itemRepository.findByPillarId(pillarId);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long projectId, Long pillarId, Long id) {
        Item item =  itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found!"));
        if (!item.getPillar().getId().equals(pillarId) || !item.getPillar().getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Item with ID " + id + " is not in pillar/project path");
        }
        return item;
    }

    @Transactional
    public Item createItem(Long projectId, Long pillarId, Item item) {
        Pillar pillar = validatePillarPath(projectId, pillarId);

        if (item instanceof Document) {
            Document doc = (Document) item;
            if (doc.getStatus() == null || doc.getStatus().isEmpty()) {
                doc.setStatus("Utworzono");
            }
            doc.setLastChangeDate(LocalDate.now());
            addHistoryEntry(doc, null, doc.getStatus(), doc.getDescription(), doc.getDeadline());
        }

        item.setAddDate(LocalDate.now());
        item.setPillar(pillar);
        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long projectId, Long pillarId, Long itemId, Item updatedItemData) {
        Item existingItem = getItemById(projectId, pillarId, itemId);

        if (!existingItem.getClass().equals(updatedItemData.getClass())) {
            throw new IllegalArgumentException("You can't change item type.");
        }

        if (existingItem instanceof Task) {

        } else if (existingItem instanceof Meeting) {

        } else if (existingItem instanceof Document existingDoc) {
            Document updatedDoc = (Document) updatedItemData;

            String oldStatus = existingDoc.getStatus();
            String newStatus = updatedDoc.getStatus();

            if (newStatus != null && !newStatus.equals(oldStatus)) {
                addHistoryEntry(existingDoc, oldStatus, newStatus, updatedDoc.getDescription(), updatedDoc.getDeadline());
                existingDoc.setStatus(newStatus);
            }

            existingDoc.setDeadline(updatedDoc.getDeadline());
            existingDoc.setDescription(updatedDoc.getDescription());
            existingDoc.setLastChangeDate(LocalDate.now());
            existingDoc.setName(updatedDoc.getName());
            existingDoc.setStatus(updatedDoc.getStatus());
        }

        return itemRepository.save(existingItem);
    }

    @Transactional
    public Item archiveItem(Long projectId, Long pillarId, Long id){
        Item archivedItem = getItemById(projectId, pillarId, id);
        archivedItem.setState("archived");
        return itemRepository.save(archivedItem);
    }

    @Transactional
    public Item finishItem(Long projectId, Long pillarId, Long id){
        Item finishedItem = getItemById(projectId, pillarId, id);
        finishedItem.setState("finished");
        return itemRepository.save(finishedItem);
    }

    private void addHistoryEntry(Document document, String oldStatus, String newStatus, String description, LocalDate deadline) {
        DocumentHistory historyEntry = new DocumentHistory(document, oldStatus, newStatus, description, deadline);

        document.getHistoryEntries().add(historyEntry);
    }
}
