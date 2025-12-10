package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarRepository;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        checkForItemDuplicates(inputItem.getName(), "active", pillarId, inputItem.getPriority());

        Item createdItem = new Item();

        setChangableFields(inputItem, createdItem);
        createdItem.setPillar(pillar);

        if (inputItem.getTags() != null && !inputItem.getTags().isEmpty()) {
            Set<Tag> tagsToAdd = new HashSet<>();

            for (var tagDto : inputItem.getTags()) {
                Tag tag = tagRepository.findById(tagDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagDto.getId()));
                tagsToAdd.add(tag);
            }

            createdItem.setTags(tagsToAdd);
        }

        return itemRepository.save(createdItem);
    }

    @Transactional
    public Item updateItem(Long projectId, Long pillarId, Long itemId, Item updatedItem) {
        Item existingItem = getItemById(projectId, pillarId, itemId);

        setChangableFields(updatedItem, existingItem);

        if (updatedItem.getTags() != null) {
            Set<Tag> updatedTags = new HashSet<>();

            for (var tagDto : updatedItem.getTags()) {
                if (tagDto.getId() != null) {
                    Tag tag = tagRepository.findById(tagDto.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagDto.getId()));
                    updatedTags.add(tag);
                }
            }

            existingItem.setTags(updatedTags);
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

    @Transactional
    public ItemHistory addHistoryEntry(Long projectId, Long pillarId, Long itemId, ItemHistory itemHistory) {
        Item item = getItemById(projectId, pillarId, itemId);

        ItemHistory historyEntry = new ItemHistory();

        historyEntry.setItem(item);
        historyEntry.setChangeDate(LocalDate.now());
        historyEntry.setDescription(itemHistory.getDescription());
        historyEntry.setGoogleFileId(itemHistory.getGoogleFileId());
        historyEntry.setWebViewLink(itemHistory.getWebViewLink());
        historyEntry.setAuthor(itemHistory.getAuthor());

        item.getHistoryEntries().add(historyEntry);

        return historyEntry;
    }

    private void setChangableFields(Item inputItem, Item existingItem){
        existingItem.setName(inputItem.getName());
        existingItem.setStatus(inputItem.getStatus());
        existingItem.setPriority(inputItem.getPriority());
        existingItem.setDeadline(inputItem.getDeadline());
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

    @Transactional
    public List<ItemHistory> getItemHistoryByItemId(Long projectId, Long pillarId, Long itemId) {
        Item item = getItemById(projectId, pillarId, itemId);
        List<ItemHistory> history = item.getHistoryEntries();

        return history;
    }

    public List<Item> searchItems(SearchingCriteria criteria) {
        Specification<Item> spec = ItemSpecifications.createSearch(criteria);
        return itemRepository.findAll(spec);
    }

    private void checkForItemDuplicates(String name, String state, Long pillarId, Integer priority) {
        if (itemRepository.existsByNameAndStateAndPillarIdAndPriority(name, state, pillarId, priority)) {
            throw new IllegalArgumentException("Item with name '" + name + "' already exists in this pillar!");
        }
    }
}
