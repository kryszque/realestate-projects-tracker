package com.mcdevka.realestate_projects_tracker.domain.item;

import com.mcdevka.realestate_projects_tracker.domain.company.Company;
import com.mcdevka.realestate_projects_tracker.domain.pillar.Pillar;
import com.mcdevka.realestate_projects_tracker.domain.pillar.PillarRepository;
import com.mcdevka.realestate_projects_tracker.domain.project.access.ProjectPermissions;
import com.mcdevka.realestate_projects_tracker.domain.searching.SearchingCriteria;
import com.mcdevka.realestate_projects_tracker.domain.tag.Tag;
import com.mcdevka.realestate_projects_tracker.domain.tag.TagRepository;
import com.mcdevka.realestate_projects_tracker.infrastructure.drive.GoogleDriveService;
import com.mcdevka.realestate_projects_tracker.security.annotation.CheckAccess;
import com.mcdevka.realestate_projects_tracker.security.annotation.ProjectId;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final PillarRepository pillarRepository;
    private final TagRepository tagRepository;
    private final GoogleDriveService googleDriveService;

    public ItemService(ItemRepository itemRepository, PillarRepository pillarRepository, TagRepository tagRepository, GoogleDriveService googleDriveService) {
        this.itemRepository = itemRepository;
        this.pillarRepository = pillarRepository;
        this.tagRepository = tagRepository;
        this.googleDriveService = googleDriveService;
    }

    private Pillar validatePillarPath(Long projectId, Long pillarId) {
        Pillar pillar = pillarRepository.findById(pillarId)
                .orElseThrow(() -> new IllegalArgumentException("Filar o ID " + pillarId + " nie istnieje."));

        if (!pillar.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Filar o ID " + pillarId + " nie należy do projektu o ID " + projectId);
        }
        return pillar;
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    @Transactional(readOnly = true)
    public List<Item> getItemsForPillar(@ProjectId Long projectId, Long pillarId) {
        validatePillarPath(projectId, pillarId);

        return itemRepository.findByPillarId(pillarId);
    }

    @CheckAccess(ProjectPermissions.CAN_VIEW)
    @Transactional(readOnly = true)
    public Item getItemById(@ProjectId Long projectId, Long pillarId, Long id) {
        Item item =  itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found!"));
        if (!item.getPillar().getId().equals(pillarId) || !item.getPillar().getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Item with ID " + id + " is not in pillar/project path");
        }
        return item;
    }

    @CheckAccess(ProjectPermissions.CAN_CREATE)
    @Transactional
    public Item createItem(@ProjectId Long projectId, Long pillarId, Item inputItem) {
        Pillar pillar = validatePillarPath(projectId, pillarId);

        checkForItemDuplicates(inputItem.getName(),"active" , inputItem.getCompany(), inputItem.getPersonResponsible(), pillarId, inputItem.getPriority());

        Item createdItem = new Item();

        createdItem.setCompany(pillar.getCompany());

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

        try {
            if (inputItem.getCustomDriveFolderId() != null && !inputItem.getCustomDriveFolderId().isEmpty()) {
                // Użytkownik podał własny link/ID folderu (zakładamy, że podał ID)
                com.google.api.services.drive.model.File existingFolder = googleDriveService.getFolder(inputItem.getCustomDriveFolderId());
                createdItem.setDriveFolderId(existingFolder.getId());
                createdItem.setDriveFolderLink(existingFolder.getWebViewLink());
            } else {
                // Standardowe tworzenie folderu wewnątrz filaru
                String parentFolderId = pillar.getDriveFolderId();
                if(parentFolderId == null) parentFolderId = googleDriveService.getRootFolderId(); // Fallback

                com.google.api.services.drive.model.File folder =
                        googleDriveService.createFolder(createdItem.getName(), parentFolderId);
                createdItem.setDriveFolderId(folder.getId());
                createdItem.setDriveFolderLink(folder.getWebViewLink());
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas konfiguracji folderu Google Drive", e);
        }

        return itemRepository.save(createdItem);
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    @Transactional
    public Item updateItem(@ProjectId Long projectId, Long pillarId, Long itemId, Item updatedItem) {
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

    @CheckAccess(ProjectPermissions.CAN_DELETE)
    @Transactional
    public Item archiveItem(@ProjectId Long projectId, Long pillarId, Long id){
        Item archivedItem = getItemById(projectId, pillarId, id); //

        // Dodanie prefiksu do nazwy, jeśli jeszcze go nie ma
        if (!archivedItem.getName().startsWith("[zarchiwizowany]")) {
            archivedItem.setName("[zarchiwizowany] " + archivedItem.getName());
        }

        archivedItem.setState("archived"); //
        return itemRepository.save(archivedItem); //
    }

    @CheckAccess(ProjectPermissions.CAN_EDIT)
    @Transactional
    public Item finishItem(@ProjectId Long projectId, Long pillarId, Long id){
        Item finishedItem = getItemById(projectId, pillarId, id);
        finishedItem.setState("finished");
        return itemRepository.save(finishedItem);
    }

    @Transactional(readOnly = true)
    public Optional<ItemHistory> getItemHistoryById( Long itemId, Long id) {
        Optional<ItemHistory> itemHistory = Optional.ofNullable(itemRepository.findHistoryByIdAndItemId(itemId, id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found!")));

        return itemHistory;
    }

    @Transactional
    public ItemHistory addHistoryEntry(@ProjectId Long projectId, Long pillarId, Long itemId, ItemHistory itemHistory) {
        Item item = getItemById(projectId, pillarId, itemId);

        ItemHistory historyEntry = new ItemHistory();

        historyEntry.setItem(item);
        historyEntry.setChangeDate(LocalDateTime.now());
        historyEntry.setDescription(itemHistory.getDescription());
        historyEntry.setGoogleFileId(itemHistory.getGoogleFileId());
        historyEntry.setWebViewLink(itemHistory.getWebViewLink());
        historyEntry.setAuthor(itemHistory.getAuthor());
        historyEntry.setPinned(false);
        historyEntry.setState("active");

        // 👇 WYKORZYSTUJEMY TWOJĄ ISTNIEJĄCĄ LOGIKĘ:
        if (itemHistory.getReplyToId() != null) {
            // 1. Pobierasz LICZBĘ (Long) z obiektu, który przyszedł z frontendu
            Long idZFrontendu = itemHistory.getReplyToId();

            // 2. Zamieniasz tę LICZBĘ na OBIEKT, używając Twojej metody
            ItemHistory obiektWiadomosci = getItemHistoryById(itemId, idZFrontendu)
                    .orElseThrow(() -> new IllegalArgumentException("Nie ma takiej wiadomości!"));

            // 3. Przypisujesz OBIEKT do nowej wiadomości
            historyEntry.setReplyTo(obiektWiadomosci);
        }

        item.getHistoryEntries().add(historyEntry);

        // Ponieważ metoda jest @Transactional, a 'item' jest zarządzany (managed),
        // nowa wiadomość zostanie zapisana w bazie automatycznie przy wyjściu z metody.
        return historyEntry;
    }

    @Transactional
    public ItemHistory updateHistoryEntry(Long projectId, Long pillarId, Long itemId, Long id, ItemHistory itemHistory) {
        ItemHistory prevHistory = getItemHistoryById(itemId, id).get();

        prevHistory.setDescription(itemHistory.getDescription());
        prevHistory.setGoogleFileId(itemHistory.getGoogleFileId());
        prevHistory.setWebViewLink(itemHistory.getWebViewLink());
        prevHistory.setAuthor(itemHistory.getAuthor());
        prevHistory.setEdited(true);

        if (itemHistory.getReplyToId() != null) {
            // Szukamy nowej wiadomości-matki po ID (Long -> ItemHistory)
            ItemHistory newReplyTo = getItemHistoryById(itemId, itemHistory.getReplyToId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wiadomości bazowej o ID: " + itemHistory.getReplyToId()));

            prevHistory.setReplyTo(newReplyTo);
        } else {
            // Jeśli z frontendu przyszło null w replyToId, usuwamy powiązanie
            prevHistory.setReplyTo(null);
        }

        return prevHistory;
    }

    @Transactional
    public ItemHistory pinOrUnPinHistory(Long projectId, Long pillarId, Long itemId, Long id) {
        ItemHistory prevHistory = getItemHistoryById(itemId, id).get();

        if (prevHistory.isPinned()) {
            prevHistory.setPinned(false);
        } else {
            prevHistory.setPinned(true);
        }

        return prevHistory;
    }

    @Transactional
    public ItemHistory archiveHistory(Long projectId, Long pillarId, Long itemId, Long id) {
        ItemHistory prevHistory = getItemHistoryById(itemId, id).get();

        prevHistory.setState("archived");

        return prevHistory;
    }

    private void setChangableFields(Item inputItem, Item existingItem){
        existingItem.setName(inputItem.getName());
        existingItem.setPersonResponsible(inputItem.getPersonResponsible());
        existingItem.setPriority(inputItem.getPriority());
        existingItem.setDeadline(inputItem.getDeadline());
    }

    @Transactional
    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Item addTagToItem(@ProjectId Long projectId, Long pillarId, Long itemId, Long tagId) {
        Item item = getItemById(projectId, pillarId, itemId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag o ID " + tagId + " nie istnieje."));

        item.getTags().add(tag);
        return itemRepository.save(item);
    }

    @Transactional
    @CheckAccess(ProjectPermissions.CAN_EDIT)
    public Item removeTagFromItem(@ProjectId Long projectId, Long pillarId, Long itemId, Long tagId) {
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
        return itemRepository.searchItems(criteria);
    }

    private void checkForItemDuplicates(String name, String state, Company personResponsible, String companyResposible, Long pillarId, Integer priority) {
        if (itemRepository.existsByNameAndStateAndCompanyAndPersonResponsibleAndPillarIdAndPriority(name, state, personResponsible, companyResposible, pillarId, priority)) {
            throw new IllegalArgumentException("Item with name '" + name + "' already exists in this pillar!");
        }
    }

    @Transactional
    public ItemHistory toggleReaction(Long itemId, Long historyId, String emoji, String userName) {
        ItemHistory history = getItemHistoryById(itemId, historyId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wiadomości"));

        Optional<MessageReaction> existingReaction = history.getReactions().stream()
                .filter(r -> r.getUserName().equals(userName)) // Szukamy po userze, nie po emotce
                .findFirst();

        if (existingReaction.isPresent()) {
            MessageReaction reaction = existingReaction.get();

            if (reaction.getEmojiCode().equals(emoji)) {
                history.getReactions().remove(reaction);
                reaction.setItemHistory(null);
            } else {
                reaction.setEmojiCode(emoji);
            }
        } else {
            MessageReaction newReaction = new MessageReaction(emoji, userName, history);
            history.getReactions().add(newReaction);
        }

        return itemRepository.save(history.getItem()).getHistoryEntries()
                .stream().filter(h -> h.getId().equals(historyId)).findFirst().get();
    }

    @Transactional(readOnly = true)
    public List<ItemHistory> getPinnedHistoryForProject(Long projectId) {
        return itemRepository.findPinnedByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<ItemHistory> getPinnedHistoryForPillar(Long pillarId) {
        return itemRepository.findPinnedByPillarId(pillarId);
    }
}
