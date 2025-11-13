package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.Project;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final PillarRepository pillarRepository;
    private final TagRepository tagRepository;

    public ItemService(ItemRepository itemRepository, PillarRepository pillarRepository, TagRepository tagRepository) {
        this.itemRepository = itemRepository;
        this.pillarRepository = pillarRepository;
        this.tagRepository = tagRepository;
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
    public Item createItem(Long projectId, Long pillarId, Item inputItem) {
        Pillar pillar = validatePillarPath(projectId, pillarId);

        Item createdItem = new Item();

        createdItem.setWebViewLink(inputItem.getWebViewLink());
        createdItem.setGoogleFileId(inputItem.getGoogleFileId());

        setChangableFields(inputItem, createdItem);
        createdItem.setPillar(pillar);

        addHistoryEntry(createdItem, createdItem.getName(), createdItem.getStatus(), createdItem.getDescription(), createdItem.getDeadline());

        return itemRepository.save(createdItem);
    }

    @Transactional
    public Item updateItem(Long projectId, Long pillarId, Long itemId, Item updatedItem) {
        Item existingItem = getItemById(projectId, pillarId, itemId);

        setChangableFields(updatedItem, existingItem);
        addHistoryEntry(existingItem, existingItem.getName(), existingItem.getStatus(), existingItem.getDescription(), existingItem.getDeadline());

        existingItem.setLastChangeDate(LocalDate.now());

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

    private void addHistoryEntry(Item item, String name, String status, String description, LocalDate deadline) {
        ItemHistory historyEntry = new ItemHistory(item, name, status, description, deadline);

        item.getHistoryEntries().add(historyEntry);
    }

    private void setChangableFields(Item inputItem, Item existingItem){
        existingItem.setName(inputItem.getName());
        existingItem.setStatus(inputItem.getStatus());
        existingItem.setDeadline(inputItem.getDeadline());
        existingItem.setDescription(inputItem.getDescription());
        existingItem.setLastChangeDate(LocalDate.now());
    }

    @Transactional
    public Item addTagToItem(Long projectId, Long pillarId, Long itemId, Long tagId) {
        Item item = getItemById(projectId, pillarId, itemId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        item.getTags().add(tag);
        return itemRepository.save(item);
    }

    @Transactional
    public Item removeTagFromItem(Long projectId, Long pillarId, Long itemId, Long tagId) {
        Item item = getItemById(projectId, pillarId, itemId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        item.getTags().remove(tag);
        return itemRepository.save(item);
    }
}
